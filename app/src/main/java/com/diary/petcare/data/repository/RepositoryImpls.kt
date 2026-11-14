package com.diary.petcare.data.repository

import com.diary.petcare.data.local.dao.ExpenseDao
import com.diary.petcare.data.local.dao.FeedingDao
import com.diary.petcare.data.local.dao.GroomingDao
import com.diary.petcare.data.local.dao.MedicineDao
import com.diary.petcare.data.local.dao.NoteDao
import com.diary.petcare.data.local.dao.PetDao
import com.diary.petcare.data.local.dao.PhotoDao
import com.diary.petcare.data.local.dao.ReminderDao
import com.diary.petcare.data.local.dao.VaccineDao
import com.diary.petcare.data.local.dao.VetVisitDao
import com.diary.petcare.data.local.dao.WeightDao
import com.diary.petcare.data.local.entity.MedicineDoseEntity
import com.diary.petcare.data.local.entity.toDomain
import com.diary.petcare.data.local.entity.toEntity
import com.diary.petcare.domain.model.Expense
import com.diary.petcare.domain.model.FeedingLog
import com.diary.petcare.domain.model.FeedingSchedule
import com.diary.petcare.domain.model.Grooming
import com.diary.petcare.domain.model.Medicine
import com.diary.petcare.domain.model.Note
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.domain.model.Photo
import com.diary.petcare.domain.model.Reminder
import com.diary.petcare.domain.model.ReminderType
import com.diary.petcare.domain.model.Vaccine
import com.diary.petcare.domain.model.VetVisit
import com.diary.petcare.domain.model.WeightLog
import com.diary.petcare.domain.repository.ExpenseRepository
import com.diary.petcare.domain.repository.FeedingRepository
import com.diary.petcare.domain.repository.GroomingRepository
import com.diary.petcare.domain.repository.MedicineRepository
import com.diary.petcare.domain.repository.NoteRepository
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.domain.repository.PhotoRepository
import com.diary.petcare.domain.repository.ReminderRepository
import com.diary.petcare.domain.repository.VaccineRepository
import com.diary.petcare.domain.repository.VetVisitRepository
import com.diary.petcare.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(private val dao: PetDao) : PetRepository {
    override fun observePets() = dao.observeAll().map { list -> list.map { it.toDomain() } }
    override fun observePet(id: Long) = dao.observe(id).map { it?.toDomain() }
    override suspend fun getPet(id: Long) = dao.get(id)?.toDomain()
    override suspend fun getPets() = dao.getAll().map { it.toDomain() }
    override suspend fun save(pet: Pet): Long =
        if (pet.id == 0L) dao.insert(pet.toEntity()) else pet.id.also { dao.update(pet.toEntity()) }
    override suspend fun delete(id: Long) = dao.delete(id)
}

@Singleton
class FeedingRepositoryImpl @Inject constructor(private val dao: FeedingDao) : FeedingRepository {
    override fun observeSchedules(petId: Long) = dao.observeSchedules(petId).map { l -> l.map { it.toDomain() } }
    override fun observeAllSchedules() = dao.observeAllSchedules().map { l -> l.map { it.toDomain() } }
    override suspend fun getSchedule(id: Long) = dao.getSchedule(id)?.toDomain()
    override suspend fun saveSchedule(schedule: FeedingSchedule): Long =
        if (schedule.id == 0L) dao.insertSchedule(schedule.toEntity())
        else schedule.id.also { dao.updateSchedule(schedule.toEntity()) }
    override suspend fun deleteSchedule(id: Long) = dao.deleteSchedule(id)
    override fun observeLogs(petId: Long) = dao.observeLogs(petId).map { l -> l.map { it.toDomain() } }
    override fun observeLogsBetween(from: Long, to: Long) =
        dao.observeLogsBetween(from, to).map { l -> l.map { it.toDomain() } }
    override suspend fun getLogs(petId: Long) = dao.getLogs(petId).map { it.toDomain() }
    override suspend fun addLog(log: FeedingLog) = dao.insertLog(log.toEntity())
    override suspend fun deleteLog(id: Long) = dao.deleteLog(id)
}

@Singleton
class VaccineRepositoryImpl @Inject constructor(private val dao: VaccineDao) : VaccineRepository {
    override fun observeForPet(petId: Long) = dao.observeForPet(petId).map { l -> l.map { it.toDomain() } }
    override suspend fun getForPet(petId: Long) = dao.getForPet(petId).map { it.toDomain() }
    override fun observeDueBefore(until: Long) = dao.observeDueBefore(until).map { l -> l.map { it.toDomain() } }
    override suspend fun get(id: Long) = dao.get(id)?.toDomain()
    override suspend fun save(vaccine: Vaccine): Long =
        if (vaccine.id == 0L) dao.insert(vaccine.toEntity()) else vaccine.id.also { dao.update(vaccine.toEntity()) }
    override suspend fun delete(id: Long) = dao.delete(id)
}

@Singleton
class MedicineRepositoryImpl @Inject constructor(private val dao: MedicineDao) : MedicineRepository {
    override fun observeForPet(petId: Long) = dao.observeForPet(petId).map { l -> l.map { it.toDomain() } }
    override suspend fun getForPet(petId: Long) = dao.getForPet(petId).map { it.toDomain() }
    override fun observeActiveOn(dayStart: Long, dayEnd: Long) =
        dao.observeActiveOn(dayStart, dayEnd).map { l -> l.map { it.toDomain() } }
    override suspend fun get(id: Long) = dao.get(id)?.toDomain()
    override suspend fun save(medicine: Medicine): Long =
        if (medicine.id == 0L) dao.insert(medicine.toEntity()) else medicine.id.also { dao.update(medicine.toEntity()) }
    override suspend fun delete(id: Long) = dao.delete(id)
    override fun observeDosesBetween(from: Long, to: Long) =
        dao.observeDosesBetween(from, to).map { l -> l.map { it.toDomain() } }
    override fun observeDoses(medicineId: Long) = dao.observeDoses(medicineId).map { l -> l.map { it.toDomain() } }
    override suspend fun setDoseGiven(medicineId: Long, petId: Long, scheduledAt: Long, givenAt: Long?) {
        dao.insertDose(MedicineDoseEntity(medicineId = medicineId, petId = petId, scheduledAt = scheduledAt, givenAt = null))
        dao.setGiven(medicineId, scheduledAt, givenAt)
    }
}

@Singleton
class VetVisitRepositoryImpl @Inject constructor(private val dao: VetVisitDao) : VetVisitRepository {
    override fun observeForPet(petId: Long) = dao.observeForPet(petId).map { l -> l.map { it.toDomain() } }
    override suspend fun getForPet(petId: Long) = dao.getForPet(petId).map { it.toDomain() }
    override fun observeNextVisitsBefore(until: Long) =
        dao.observeNextVisitsBefore(until).map { l -> l.map { it.toDomain() } }
    override suspend fun get(id: Long) = dao.get(id)?.toDomain()
    override suspend fun save(visit: VetVisit): Long =
        if (visit.id == 0L) dao.insert(visit.toEntity()) else visit.id.also { dao.update(visit.toEntity()) }
    override suspend fun delete(id: Long) = dao.delete(id)
}

@Singleton
class WeightRepositoryImpl @Inject constructor(private val dao: WeightDao) : WeightRepository {
    override fun observeForPet(petId: Long) = dao.observeForPet(petId).map { l -> l.map { it.toDomain() } }
    override suspend fun getForPet(petId: Long) = dao.getForPet(petId).map { it.toDomain() }
    override suspend fun get(id: Long) = dao.get(id)?.toDomain()
    override suspend fun save(log: WeightLog): Long =
        if (log.id == 0L) dao.insert(log.toEntity()) else log.id.also { dao.update(log.toEntity()) }
    override suspend fun delete(id: Long) = dao.delete(id)
}

@Singleton
class GroomingRepositoryImpl @Inject constructor(private val dao: GroomingDao) : GroomingRepository {
    override fun observeForPet(petId: Long) = dao.observeForPet(petId).map { l -> l.map { it.toDomain() } }
    override fun observeAll() = dao.observeAll().map { l -> l.map { it.toDomain() } }
    override suspend fun get(id: Long) = dao.get(id)?.toDomain()
    override suspend fun save(grooming: Grooming): Long =
        if (grooming.id == 0L) dao.insert(grooming.toEntity()) else grooming.id.also { dao.update(grooming.toEntity()) }
    override suspend fun delete(id: Long) = dao.delete(id)
}

@Singleton
class ExpenseRepositoryImpl @Inject constructor(private val dao: ExpenseDao) : ExpenseRepository {
    override fun observeForPet(petId: Long) = dao.observeForPet(petId).map { l -> l.map { it.toDomain() } }
    override fun observeAll() = dao.observeAll().map { l -> l.map { it.toDomain() } }
    override suspend fun get(id: Long) = dao.get(id)?.toDomain()
    override suspend fun save(expense: Expense): Long =
        if (expense.id == 0L) dao.insert(expense.toEntity()) else expense.id.also { dao.update(expense.toEntity()) }
    override suspend fun delete(id: Long) = dao.delete(id)
}

@Singleton
class NoteRepositoryImpl @Inject constructor(private val dao: NoteDao) : NoteRepository {
    override fun observeForPet(petId: Long) = dao.observeForPet(petId).map { l -> l.map { it.toDomain() } }
    override suspend fun get(id: Long) = dao.get(id)?.toDomain()
    override suspend fun save(note: Note): Long =
        if (note.id == 0L) dao.insert(note.toEntity()) else note.id.also { dao.update(note.toEntity()) }
    override suspend fun delete(id: Long) = dao.delete(id)
}

@Singleton
class PhotoRepositoryImpl @Inject constructor(private val dao: PhotoDao) : PhotoRepository {
    override fun observeForPet(petId: Long) = dao.observeForPet(petId).map { l -> l.map { it.toDomain() } }
    override suspend fun get(id: Long) = dao.get(id)?.toDomain()
    override suspend fun save(photo: Photo): Long =
        if (photo.id == 0L) dao.insert(photo.toEntity()) else photo.id.also { dao.update(photo.toEntity()) }
    override suspend fun delete(id: Long) = dao.delete(id)
}

@Singleton
class ReminderRepositoryImpl @Inject constructor(private val dao: ReminderDao) : ReminderRepository {
    override fun observeAll(): Flow<List<Reminder>> = dao.observeAll().map { l -> l.map { it.toDomain() } }
    override suspend fun getEnabled() = dao.getEnabled().map { it.toDomain() }
    override suspend fun get(id: Long) = dao.get(id)?.toDomain()
    override suspend fun getByRef(type: ReminderType, refId: Long) = dao.getByRef(type, refId).map { it.toDomain() }
    override suspend fun getByRefSlot(type: ReminderType, refId: Long, slot: Int) =
        dao.getByRefSlot(type, refId, slot)?.toDomain()
    override suspend fun save(reminder: Reminder): Long =
        if (reminder.id == 0L) dao.insert(reminder.toEntity()) else reminder.id.also { dao.update(reminder.toEntity()) }
    override suspend fun delete(id: Long) = dao.delete(id)
}
