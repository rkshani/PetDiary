package com.diary.petcare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.diary.petcare.data.local.entity.MedicineDoseEntity
import com.diary.petcare.data.local.entity.MedicineEntity
import com.diary.petcare.data.local.entity.ReminderEntity
import com.diary.petcare.data.local.entity.VaccineEntity
import com.diary.petcare.data.local.entity.VetVisitEntity
import com.diary.petcare.domain.model.ReminderType
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccineDao {
    @Query("SELECT * FROM vaccines WHERE petId = :petId ORDER BY COALESCE(nextDueDate, dateGiven, 0) DESC")
    fun observeForPet(petId: Long): Flow<List<VaccineEntity>>

    @Query("SELECT * FROM vaccines WHERE petId = :petId ORDER BY COALESCE(dateGiven, nextDueDate, 0)")
    suspend fun getForPet(petId: Long): List<VaccineEntity>

    /** Vaccines with a next due date on or before [until] (includes overdue ones). */
    @Query("SELECT * FROM vaccines WHERE nextDueDate IS NOT NULL AND nextDueDate <= :until ORDER BY nextDueDate")
    fun observeDueBefore(until: Long): Flow<List<VaccineEntity>>

    @Query("SELECT * FROM vaccines WHERE id = :id")
    suspend fun get(id: Long): VaccineEntity?

    @Insert
    suspend fun insert(vaccine: VaccineEntity): Long

    @Update
    suspend fun update(vaccine: VaccineEntity)

    @Query("DELETE FROM vaccines WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE petId = :petId ORDER BY startDate DESC")
    fun observeForPet(petId: Long): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE petId = :petId ORDER BY startDate")
    suspend fun getForPet(petId: Long): List<MedicineEntity>

    /** Courses that are running on [day] (any time inside that day). */
    @Query("SELECT * FROM medicines WHERE startDate <= :dayEnd AND (endDate IS NULL OR endDate >= :dayStart)")
    fun observeActiveOn(dayStart: Long, dayEnd: Long): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE startDate <= :dayEnd AND (endDate IS NULL OR endDate >= :dayStart)")
    suspend fun getActiveOn(dayStart: Long, dayEnd: Long): List<MedicineEntity>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun get(id: Long): MedicineEntity?

    @Insert
    suspend fun insert(medicine: MedicineEntity): Long

    @Update
    suspend fun update(medicine: MedicineEntity)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM medicine_doses WHERE scheduledAt BETWEEN :from AND :to ORDER BY scheduledAt")
    fun observeDosesBetween(from: Long, to: Long): Flow<List<MedicineDoseEntity>>

    @Query("SELECT * FROM medicine_doses WHERE medicineId = :medicineId ORDER BY scheduledAt DESC")
    fun observeDoses(medicineId: Long): Flow<List<MedicineDoseEntity>>

    @Query("SELECT * FROM medicine_doses WHERE medicineId = :medicineId AND scheduledAt = :scheduledAt")
    suspend fun getDose(medicineId: Long, scheduledAt: Long): MedicineDoseEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDose(dose: MedicineDoseEntity): Long

    @Query("UPDATE medicine_doses SET givenAt = :givenAt WHERE medicineId = :medicineId AND scheduledAt = :scheduledAt")
    suspend fun setGiven(medicineId: Long, scheduledAt: Long, givenAt: Long?)
}

@Dao
interface VetVisitDao {
    @Query("SELECT * FROM vet_visits WHERE petId = :petId ORDER BY date DESC")
    fun observeForPet(petId: Long): Flow<List<VetVisitEntity>>

    @Query("SELECT * FROM vet_visits WHERE petId = :petId ORDER BY date")
    suspend fun getForPet(petId: Long): List<VetVisitEntity>

    @Query("SELECT * FROM vet_visits WHERE nextVisitDate IS NOT NULL AND nextVisitDate <= :until ORDER BY nextVisitDate")
    fun observeNextVisitsBefore(until: Long): Flow<List<VetVisitEntity>>

    @Query("SELECT * FROM vet_visits WHERE id = :id")
    suspend fun get(id: Long): VetVisitEntity?

    @Insert
    suspend fun insert(visit: VetVisitEntity): Long

    @Update
    suspend fun update(visit: VetVisitEntity)

    @Query("DELETE FROM vet_visits WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY triggerAt")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE enabled = 1")
    suspend fun getEnabled(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun get(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE type = :type AND refId = :refId ORDER BY slot")
    suspend fun getByRef(type: ReminderType, refId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE type = :type AND refId = :refId AND slot = :slot")
    suspend fun getByRefSlot(type: ReminderType, refId: Long, slot: Int): ReminderEntity?

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Long)
}
