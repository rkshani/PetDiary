package com.diary.petcare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diary.petcare.data.local.entity.ExpenseEntity
import com.diary.petcare.data.local.entity.FeedingLogEntity
import com.diary.petcare.data.local.entity.FeedingScheduleEntity
import com.diary.petcare.data.local.entity.GroomingEntity
import com.diary.petcare.data.local.entity.NoteEntity
import com.diary.petcare.data.local.entity.PhotoEntity
import com.diary.petcare.data.local.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingDao {
    @Query("SELECT * FROM feeding_schedules WHERE petId = :petId ORDER BY timeOfDay")
    fun observeSchedules(petId: Long): Flow<List<FeedingScheduleEntity>>

    @Query("SELECT * FROM feeding_schedules ORDER BY timeOfDay")
    fun observeAllSchedules(): Flow<List<FeedingScheduleEntity>>

    @Query("SELECT * FROM feeding_schedules WHERE id = :id")
    suspend fun getSchedule(id: Long): FeedingScheduleEntity?

    @Insert
    suspend fun insertSchedule(schedule: FeedingScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: FeedingScheduleEntity)

    @Query("DELETE FROM feeding_schedules WHERE id = :id")
    suspend fun deleteSchedule(id: Long)

    @Query("SELECT * FROM feeding_logs WHERE petId = :petId ORDER BY fedAt DESC")
    fun observeLogs(petId: Long): Flow<List<FeedingLogEntity>>

    @Query("SELECT * FROM feeding_logs WHERE fedAt BETWEEN :from AND :to ORDER BY fedAt DESC")
    fun observeLogsBetween(from: Long, to: Long): Flow<List<FeedingLogEntity>>

    @Query("SELECT * FROM feeding_logs WHERE petId = :petId ORDER BY fedAt DESC")
    suspend fun getLogs(petId: Long): List<FeedingLogEntity>

    @Insert
    suspend fun insertLog(log: FeedingLogEntity): Long

    @Query("DELETE FROM feeding_logs WHERE id = :id")
    suspend fun deleteLog(id: Long)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs WHERE petId = :petId ORDER BY date")
    fun observeForPet(petId: Long): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs WHERE petId = :petId ORDER BY date")
    suspend fun getForPet(petId: Long): List<WeightLogEntity>

    @Query("SELECT * FROM weight_logs WHERE id = :id")
    suspend fun get(id: Long): WeightLogEntity?

    @Insert
    suspend fun insert(log: WeightLogEntity): Long

    @Update
    suspend fun update(log: WeightLogEntity)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface GroomingDao {
    @Query("SELECT * FROM groomings WHERE petId = :petId ORDER BY date DESC")
    fun observeForPet(petId: Long): Flow<List<GroomingEntity>>

    @Query("SELECT * FROM groomings ORDER BY date DESC")
    fun observeAll(): Flow<List<GroomingEntity>>

    @Query("SELECT * FROM groomings WHERE id = :id")
    suspend fun get(id: Long): GroomingEntity?

    @Insert
    suspend fun insert(grooming: GroomingEntity): Long

    @Update
    suspend fun update(grooming: GroomingEntity)

    @Query("DELETE FROM groomings WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE petId = :petId ORDER BY date DESC")
    fun observeForPet(petId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun get(id: Long): ExpenseEntity?

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE petId = :petId ORDER BY date DESC")
    fun observeForPet(petId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun get(id: Long): NoteEntity?

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE petId = :petId ORDER BY takenAt DESC")
    fun observeForPet(petId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun get(id: Long): PhotoEntity?

    @Query("SELECT path FROM photos")
    suspend fun getAllPaths(): List<String>

    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Update
    suspend fun update(photo: PhotoEntity)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun delete(id: Long)
}
