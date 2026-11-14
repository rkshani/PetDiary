package com.diary.petcare.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import com.diary.petcare.core.Constants
import com.diary.petcare.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Copies picked or captured photos into the app's private folder, shrinks them, and
 * keeps only the file path in the database.
 */
@Singleton
class PhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    val photosDir: File get() = File(context.filesDir, Constants.PHOTOS_DIR).apply { mkdirs() }

    /** Copies [uri] into the photos folder. Returns the saved file path or null on failure. */
    suspend fun import(uri: Uri): String? = withContext(io) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= MAX_SIDE || bounds.outHeight / (sample * 2) >= MAX_SIDE) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val raw = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                ?: return@runCatching null
            val rotated = rotateByExif(uri, raw)
            val scaled = scaleDown(rotated)
            val file = File(photosDir, "${UUID.randomUUID()}.jpg")
            file.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
            file.absolutePath
        }.getOrNull()
    }

    /** A fresh Uri for the camera app to write into. */
    fun newCameraUri(): Uri {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    suspend fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        withContext(io) { runCatching { File(path).takeIf { it.parentFile == photosDir }?.delete() } }
    }

    private fun rotateByExif(uri: Uri, bmp: Bitmap): Bitmap {
        val degrees = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                when (ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        }.getOrDefault(0f)
        if (degrees == 0f) return bmp
        val m = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }

    private fun scaleDown(bmp: Bitmap): Bitmap {
        val longest = maxOf(bmp.width, bmp.height)
        if (longest <= MAX_SIDE) return bmp
        val f = MAX_SIDE.toFloat() / longest
        return Bitmap.createScaledBitmap(bmp, (bmp.width * f).toInt(), (bmp.height * f).toInt(), true)
    }

    private companion object {
        const val MAX_SIDE = 1600
        const val QUALITY = 85
    }
}
