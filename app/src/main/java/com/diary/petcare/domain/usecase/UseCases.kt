package com.diary.petcare.domain.usecase

import com.diary.petcare.domain.model.FeedingLog
import com.diary.petcare.domain.model.FeedingSchedule
import com.diary.petcare.domain.model.Grooming
import com.diary.petcare.domain.model.Medicine
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.domain.model.ReminderType
import com.diary.petcare.domain.model.Vaccine
import com.diary.petcare.domain.model.VetVisit
import com.diary.petcare.domain.reminder.ReminderSync
import com.diary.petcare.domain.repository.FeedingRepository
import com.diary.petcare.domain.repository.GroomingRepository
import com.diary.petcare.domain.repository.MedicineRepository
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.domain.repository.VaccineRepository
import com.diary.petcare.domain.repository.VetVisitRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/*
 * Use cases hold the rules that touch more than one table, like keeping reminders in step.
 * Plain reads and simple saves (weight, expense, note, photo) go straight to the repository.
 */

class SavePetUseCase @Inject constructor(private val repo: PetRepository) {
    suspend operator fun invoke(pet: Pet): Long {
        require(pet.name.isNotBlank()) { "Pet name is required" }
        return repo.save(pet.copy(name = pet.name.trim()))
    }
}

class DeletePetUseCase @Inject constructor(
    private val repo: PetRepository,
    private val feeding: FeedingRepository,
    private val medicines: MedicineRepository,
    private val vaccines: VaccineRepository,
    private val vetVisits: VetVisitRepository,
    private val groomings: GroomingRepository,
    private val sync: ReminderSync,
) {
    suspend operator fun invoke(petId: Long) {
        // Take reminders off the timer first; the rows go away with the pet (cascade).
        feeding.observeSchedules(petId).first().forEach { sync.remove(ReminderType.FEEDING, it.id) }
        medicines.getForPet(petId).forEach { sync.remove(ReminderType.MEDICINE, it.id) }
        vaccines.getForPet(petId).forEach { sync.remove(ReminderType.VACCINE, it.id) }
        vetVisits.getForPet(petId).forEach { sync.remove(ReminderType.VET, it.id) }
        groomings.observeForPet(petId).first().forEach { sync.remove(ReminderType.GROOMING, it.id) }
        repo.delete(petId)
    }
}

class SaveFeedingScheduleUseCase @Inject constructor(
    private val repo: FeedingRepository,
    private val sync: ReminderSync,
) {
    suspend operator fun invoke(schedule: FeedingSchedule): Long {
        require(schedule.food.isNotBlank()) { "Food is required" }
        val id = repo.saveSchedule(schedule)
        sync.syncFeeding(schedule.copy(id = id))
        return id
    }
}

class DeleteFeedingScheduleUseCase @Inject constructor(
    private val repo: FeedingRepository,
    private val sync: ReminderSync,
) {
    suspend operator fun invoke(id: Long) {
        sync.remove(ReminderType.FEEDING, id)
        repo.deleteSchedule(id)
    }
}

class LogFeedingUseCase @Inject constructor(private val repo: FeedingRepository) {
    suspend operator fun invoke(petId: Long, schedule: FeedingSchedule? = null, note: String = ""): Long =
        repo.addLog(
            FeedingLog(
                petId = petId,
                scheduleId = schedule?.id,
                food = schedule?.food.orEmpty(),
                quantity = schedule?.quantity.orEmpty(),
                note = note,
            ),
        )
}

class SaveVaccineUseCase @Inject constructor(
    private val repo: VaccineRepository,
    private val sync: ReminderSync,
) {
    suspend operator fun invoke(vaccine: Vaccine): Long {
        require(vaccine.name.isNotBlank()) { "Vaccine name is required" }
        val id = repo.save(vaccine)
        sync.syncVaccine(vaccine.copy(id = id))
        return id
    }
}

class DeleteVaccineUseCase @Inject constructor(
    private val repo: VaccineRepository,
    private val sync: ReminderSync,
) {
    suspend operator fun invoke(id: Long) {
        sync.remove(ReminderType.VACCINE, id)
        repo.delete(id)
    }
}

class SaveMedicineUseCase @Inject constructor(
    private val repo: MedicineRepository,
    private val sync: ReminderSync,
) {
    suspend operator fun invoke(medicine: Medicine): Long {
        require(medicine.name.isNotBlank()) { "Medicine name is required" }
        val id = repo.save(medicine)
        sync.syncMedicine(medicine.copy(id = id))
        return id
    }
}

class DeleteMedicineUseCase @Inject constructor(
    private val repo: MedicineRepository,
    private val sync: ReminderSync,
) {
    suspend operator fun invoke(id: Long) {
        sync.remove(ReminderType.MEDICINE, id)
        repo.delete(id)
    }
}

class SetDoseGivenUseCase @Inject constructor(private val repo: MedicineRepository) {
    suspend operator fun invoke(medicineId: Long, petId: Long, scheduledAt: Long, given: Boolean) =
        repo.setDoseGiven(medicineId, petId, scheduledAt, if (given) System.currentTimeMillis() else null)
}

class SaveVetVisitUseCase @Inject constructor(
    private val repo: VetVisitRepository,
    private val sync: ReminderSync,
) {
    suspend operator fun invoke(visit: VetVisit): Long {
        val id = repo.save(visit)
        sync.syncVetVisit(visit.copy(id = id))
        return id
    }
}

class DeleteVetVisitUseCase @Inject constructor(
    private val repo: VetVisitRepository,
    private val sync: ReminderSync,
) {
    suspend operator fun invoke(id: Long) {
        sync.remove(ReminderType.VET, id)
        repo.delete(id)
    }
}

class SaveGroomingUseCase @Inject constructor(
    private val repo: GroomingRepository,
    private val sync: ReminderSync,
) {
    /** Saves a grooming entry. A newer entry of the same type replaces the older one's reminder. */
    suspend operator fun invoke(grooming: Grooming, older: List<Grooming>): Long {
        val id = repo.save(grooming)
        older.filter { it.id != id && it.type == grooming.type && it.date <= grooming.date }
            .forEach { sync.remove(ReminderType.GROOMING, it.id) }
        sync.syncGrooming(grooming.copy(id = id))
        return id
    }
}

class DeleteGroomingUseCase @Inject constructor(
    private val repo: GroomingRepository,
    private val sync: ReminderSync,
) {
    suspend operator fun invoke(id: Long) {
        sync.remove(ReminderType.GROOMING, id)
        repo.delete(id)
    }
}
