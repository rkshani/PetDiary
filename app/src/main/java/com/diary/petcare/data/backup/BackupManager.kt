package com.diary.petcare.data.backup

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.diary.petcare.BuildConfig
import com.diary.petcare.core.Constants
import com.diary.petcare.data.local.PetDatabase
import com.diary.petcare.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed interface BackupResult {
    data class Done(val photos: Int) : BackupResult
    data class Failed(val reason: Reason) : BackupResult
    enum class Reason { WRITE, READ, NOT_A_BACKUP, NEWER_VERSION }
}

/**
 * Offline backup: one .zip with the Room database and all photos, written to a place the
 * user picks with the Storage Access Framework (no storage permission needed).
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: PetDatabase,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private val dbFile get() = context.getDatabasePath(Constants.DATABASE_NAME)
    private val photosDir get() = File(context.filesDir, Constants.PHOTOS_DIR).apply { mkdirs() }

    suspend fun backup(target: Uri): BackupResult = withContext(io) {
        try {
            // Fold the write-ahead log into the main file so one file holds everything.
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
            var photoCount = 0
            val out = context.contentResolver.openOutputStream(target) ?: return@withContext BackupResult.Failed(BackupResult.Reason.WRITE)
            ZipOutputStream(out.buffered()).use { zip ->
                zip.putNextEntry(ZipEntry(META))
                zip.write(meta().toString().toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry(DB_ENTRY))
                dbFile.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()

                photosDir.listFiles()?.filter { it.isFile }?.forEach { f ->
                    zip.putNextEntry(ZipEntry("$PHOTOS_PREFIX${f.name}"))
                    f.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                    photoCount++
                }
            }
            BackupResult.Done(photoCount)
        } catch (e: IOException) {
            BackupResult.Failed(BackupResult.Reason.WRITE)
        } catch (e: SecurityException) {
            BackupResult.Failed(BackupResult.Reason.WRITE)
        }
    }

    /**
     * Replaces all data with the backup. On success the caller must restart the app
     * ([restartApp]) because the database was closed.
     */
    suspend fun restore(source: Uri): BackupResult = withContext(io) {
        val staging = File(context.cacheDir, "restore").apply { deleteRecursively(); mkdirs() }
        try {
            var hasDb = false
            var meta: JSONObject? = null
            val input = context.contentResolver.openInputStream(source) ?: return@withContext BackupResult.Failed(BackupResult.Reason.READ)
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val name = entry.name
                    when {
                        entry.isDirectory -> Unit
                        name == META -> meta = JSONObject(zip.readBytes().decodeToString())
                        name == DB_ENTRY -> {
                            File(staging, DB_ENTRY).outputStream().use { zip.copyTo(it) }
                            hasDb = true
                        }
                        name.startsWith(PHOTOS_PREFIX) -> {
                            // Only plain file names: blocks "../" tricks (zip slip).
                            val fileName = name.removePrefix(PHOTOS_PREFIX)
                            if (fileName.isNotEmpty() && !fileName.contains('/') && !fileName.contains('\\') && fileName != "..") {
                                val dir = File(staging, "photos").apply { mkdirs() }
                                File(dir, fileName).outputStream().use { zip.copyTo(it) }
                            }
                        }
                    }
                    zip.closeEntry()
                }
            }
            val m = meta
            if (!hasDb || m == null || m.optString("app") != APP_ID) {
                return@withContext BackupResult.Failed(BackupResult.Reason.NOT_A_BACKUP)
            }
            if (m.optInt("schema", 0) > SCHEMA_VERSION) {
                return@withContext BackupResult.Failed(BackupResult.Reason.NEWER_VERSION)
            }

            // Point photo paths at this phone's photo folder (the app folder can differ).
            fixPhotoPaths(File(staging, DB_ENTRY))

            // Swap the files. (v2: cancel the old reminder jobs first, see app/src/v2/README.md.)
            db.close()
            listOf(dbFile, File(dbFile.path + "-wal"), File(dbFile.path + "-shm")).forEach { it.delete() }
            File(staging, DB_ENTRY).copyTo(dbFile, overwrite = true)
            photosDir.deleteRecursively()
            photosDir.mkdirs()
            File(staging, "photos").listFiles()?.forEach { it.copyTo(File(photosDir, it.name), overwrite = true) }
            val count = photosDir.listFiles()?.size ?: 0
            BackupResult.Done(count)
        } catch (e: Exception) {
            BackupResult.Failed(BackupResult.Reason.READ)
        } finally {
            staging.deleteRecursively()
        }
    }

    private fun fixPhotoPaths(file: File) {
        val prefix = photosDir.absolutePath + "/"
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use { sql ->
            listOf("pets" to "photoPath", "vaccines" to "cardPhotoPath", "vet_visits" to "prescriptionPhotoPath", "photos" to "path")
                .forEach { (table, col) ->
                    sql.execSQL(
                        "UPDATE $table SET $col = ? || substr($col, instr($col, '/photos/') + 8) WHERE $col LIKE '%/photos/%'",
                        arrayOf(prefix),
                    )
                }
        }
    }

    /** Starts the app fresh so the restored database is opened. */
    fun restartApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    private fun meta() = JSONObject().apply {
        put("app", APP_ID)
        put("schema", SCHEMA_VERSION)
        put("appVersion", BuildConfig.VERSION_NAME)
        put("createdAt", System.currentTimeMillis())
    }

    private companion object {
        const val APP_ID = "com.diary.petcare"
        const val SCHEMA_VERSION = 1
        const val META = "meta.json"
        const val DB_ENTRY = "pet_diary.db"
        const val PHOTOS_PREFIX = "photos/"
    }
}
