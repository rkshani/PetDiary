package com.diary.petcare.domain.repository

import com.diary.petcare.domain.model.Expense
import com.diary.petcare.domain.model.FeedingLog
import com.diary.petcare.domain.model.FeedingSchedule
import com.diary.petcare.domain.model.Grooming
import com.diary.petcare.domain.model.Medicine
import com.diary.petcare.domain.model.MedicineDose
import com.diary.petcare.domain.model.Note
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.domain.model.Photo
import com.diary.petcare.domain.model.Reminder
import com.diary.petcare.domain.model.ReminderType
import com.diary.petcare.domain.model.Vaccine
import com.diary.petcare.domain.model.VetVisit
import com.diary.petcare.domain.model.WeightLog
import kotlinx.coroutines.flow.Flow

/* Save functions insert when id == 0, otherwise update, and return the row id. */

interface PetRepository {
    fun observePets(): Flow<List<Pet>>
    fun observePet(id: Long): Flow<Pet?>
    suspend fun getPet(id: Long): Pet?
    suspend fun getPets(): List<Pet>
    suspend fun save(pet: Pet): Long
    suspend fun delete(id: Long)
}

interface FeedingRepository {
    fun observeSchedules(petId: Long): Flow<List<FeedingSchedule>>
    fun observeAllSchedules(): Flow<List<FeedingSchedule>>
    suspend fun getSchedule(id: Long): FeedingSchedule?
    suspend fun saveSchedule(schedule: FeedingSchedule): Long
    suspend fun deleteSchedule(id: Long)
    fun observeLogs(petId: Long): Flow<List<FeedingLog>>
    fun observeLogsBetween(from: Long, to: Long): Flow<List<FeedingLog>>
    suspend fun getLogs(petId: Long): List<FeedingLog>
    suspend fun addLog(log: FeedingLog): Long
    suspend fun deleteLog(id: Long)
}

interface VaccineRepository {
    fun observeForPet(petId: Long): Flow<List<Vaccine>>
    suspend fun getForPet(petId: Long): List<Vaccine>
    fun observeDueBefore(until: Long): Flow<List<Vaccine>>
    suspend fun get(id: Long): Vaccine?
    suspend fun save(vaccine: Vaccine): Long
    suspend fun delete(id: Long)
}

interface MedicineRepository {
    fun observeForPet(petId: Long): Flow<List<Medicine>>
    suspend fun getForPet(petId: Long): List<Medicine>
    fun observeActiveOn(dayStart: Long, dayEnd: Long): Flow<List<Medicine>>
    suspend fun get(id: Long): Medicine?
    suspend fun save(medicine: Medicine): Long
    suspend fun delete(id: Long)
    fun observeDosesBetween(from: Long, to: Long): Flow<List<MedicineDose>>
    fun observeDoses(medicineId: Long): Flow<List<MedicineDose>>
    suspend fun setDoseGiven(medicineId: Long, petId: Long, scheduledAt: Long, givenAt: Long?)
}

interface VetVisitRepository {
    fun observeForPet(petId: Long): Flow<List<VetVisit>>
    suspend fun getForPet(petId: Long): List<VetVisit>
    fun observeNextVisitsBefore(until: Long): Flow<List<VetVisit>>
    suspend fun get(id: Long): VetVisit?
    suspend fun save(visit: VetVisit): Long
    suspend fun delete(id: Long)
}

interface WeightRepository {
    fun observeForPet(petId: Long): Flow<List<WeightLog>>
    suspend fun getForPet(petId: Long): List<WeightLog>
    suspend fun get(id: Long): WeightLog?
    suspend fun save(log: WeightLog): Long
    suspend fun delete(id: Long)
}

interface GroomingRepository {
    fun observeForPet(petId: Long): Flow<List<Grooming>>
    fun observeAll(): Flow<List<Grooming>>
    suspend fun get(id: Long): Grooming?
    suspend fun save(grooming: Grooming): Long
    suspend fun delete(id: Long)
}

interface ExpenseRepository {
    fun observeForPet(petId: Long): Flow<List<Expense>>
    fun observeAll(): Flow<List<Expense>>
    suspend fun get(id: Long): Expense?
    suspend fun save(expense: Expense): Long
    suspend fun delete(id: Long)
}

interface NoteRepository {
    fun observeForPet(petId: Long): Flow<List<Note>>
    suspend fun get(id: Long): Note?
    suspend fun save(note: Note): Long
    suspend fun delete(id: Long)
}

interface PhotoRepository {
    fun observeForPet(petId: Long): Flow<List<Photo>>
    suspend fun get(id: Long): Photo?
    suspend fun save(photo: Photo): Long
    suspend fun delete(id: Long)
}

interface ReminderRepository {
    fun observeAll(): Flow<List<Reminder>>
    suspend fun getEnabled(): List<Reminder>
    suspend fun get(id: Long): Reminder?
    suspend fun getByRef(type: ReminderType, refId: Long): List<Reminder>
    suspend fun getByRefSlot(type: ReminderType, refId: Long, slot: Int): Reminder?
    suspend fun save(reminder: Reminder): Long
    suspend fun delete(id: Long)
}
