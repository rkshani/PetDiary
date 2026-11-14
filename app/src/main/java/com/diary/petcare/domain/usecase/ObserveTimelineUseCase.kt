package com.diary.petcare.domain.usecase

import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.ExpenseCategory
import com.diary.petcare.domain.model.GroomingType
import com.diary.petcare.domain.model.MedicineKind
import com.diary.petcare.domain.repository.ExpenseRepository
import com.diary.petcare.domain.repository.GroomingRepository
import com.diary.petcare.domain.repository.MedicineRepository
import com.diary.petcare.domain.repository.NoteRepository
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.domain.repository.PhotoRepository
import com.diary.petcare.domain.repository.VaccineRepository
import com.diary.petcare.domain.repository.VetVisitRepository
import com.diary.petcare.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

enum class TimelineKind { BIRTH, JOINED, VACCINE_GIVEN, VACCINE_DUE, MEDICINE, VET, WEIGHT, GROOMING, EXPENSE, NOTE, PHOTO }

/** One event in a pet's life. Text is raw; the UI adds labels and formatting. */
data class TimelineEvent(
    val kind: TimelineKind,
    val date: Long,
    val title: String = "",
    val detail: String = "",
    val refId: Long = 0,
    val amountPaisa: Long? = null,
    val weightGrams: Long? = null,
    val groomingType: GroomingType? = null,
    val medicineKind: MedicineKind? = null,
    val expenseCategory: ExpenseCategory? = null,
    val photoPath: String? = null,
)

/** All events for one pet, newest first. Future vaccine due dates are left out. */
class ObserveTimelineUseCase @Inject constructor(
    private val pets: PetRepository,
    private val vaccines: VaccineRepository,
    private val medicines: MedicineRepository,
    private val vetVisits: VetVisitRepository,
    private val weights: WeightRepository,
    private val groomings: GroomingRepository,
    private val expenses: ExpenseRepository,
    private val notes: NoteRepository,
    private val photos: PhotoRepository,
) {
    operator fun invoke(petId: Long): Flow<List<TimelineEvent>> {
        val sources: List<Flow<List<TimelineEvent>>> = listOf(
            pets.observePet(petId).map { p ->
                if (p == null) emptyList() else listOfNotNull(
                    p.birthDate?.let { TimelineEvent(TimelineKind.BIRTH, it, p.name) },
                    TimelineEvent(TimelineKind.JOINED, p.createdAt, p.name),
                )
            },
            vaccines.observeForPet(petId).map { list ->
                list.flatMap { v ->
                    listOfNotNull(
                        v.dateGiven?.let { TimelineEvent(TimelineKind.VACCINE_GIVEN, it, v.name, v.vetName, v.id) },
                        v.nextDueDate?.takeIf { v.dateGiven == null && it <= DateTimeUtils.endOfDay() }
                            ?.let { TimelineEvent(TimelineKind.VACCINE_DUE, it, v.name, refId = v.id) },
                    )
                }
            },
            medicines.observeForPet(petId).map { list ->
                list.map { TimelineEvent(TimelineKind.MEDICINE, it.startDate, it.name, it.dose, it.id, medicineKind = it.kind) }
            },
            vetVisits.observeForPet(petId).map { list ->
                list.map {
                    TimelineEvent(TimelineKind.VET, it.date, it.reason, it.diagnosis, it.id, amountPaisa = it.costPaisa.takeIf { c -> c > 0 })
                }
            },
            weights.observeForPet(petId).map { list ->
                list.map { TimelineEvent(TimelineKind.WEIGHT, it.date, detail = it.note, refId = it.id, weightGrams = it.weightGrams) }
            },
            groomings.observeForPet(petId).map { list ->
                list.map { TimelineEvent(TimelineKind.GROOMING, it.date, detail = it.note, refId = it.id, groomingType = it.type) }
            },
            expenses.observeForPet(petId).map { list ->
                list.map {
                    TimelineEvent(TimelineKind.EXPENSE, it.date, detail = it.note, refId = it.id, amountPaisa = it.amountPaisa, expenseCategory = it.category)
                }
            },
            notes.observeForPet(petId).map { list -> list.map { TimelineEvent(TimelineKind.NOTE, it.date, it.title, it.text, it.id) } },
            photos.observeForPet(petId).map { list ->
                list.map { TimelineEvent(TimelineKind.PHOTO, it.takenAt, detail = it.caption, refId = it.id, photoPath = it.path) }
            },
        )
        return combine(sources) { parts -> parts.flatMap { it }.sortedByDescending { it.date } }
    }
}
