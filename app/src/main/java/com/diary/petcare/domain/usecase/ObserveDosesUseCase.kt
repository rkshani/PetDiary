package com.diary.petcare.domain.usecase

import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.Medicine
import com.diary.petcare.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** One planned dose of a medicine on a given day. */
data class DoseItem(val medicine: Medicine, val scheduledAt: Long, val givenAt: Long?) {
    val given: Boolean get() = givenAt != null
}

/**
 * Lists every dose planned for the day of [dayMillis], with its "given" state.
 * Pass [petId] to limit it to one pet.
 */
class ObserveDosesUseCase @Inject constructor(private val repo: MedicineRepository) {
    operator fun invoke(dayMillis: Long = DateTimeUtils.now(), petId: Long? = null): Flow<List<DoseItem>> {
        val start = DateTimeUtils.startOfDay(dayMillis)
        val end = DateTimeUtils.endOfDay(dayMillis)
        return combine(repo.observeActiveOn(start, end), repo.observeDosesBetween(start, end)) { meds, doses ->
            meds.filter { petId == null || it.petId == petId }
                .flatMap { m ->
                    m.timesOfDay.map { t ->
                        val at = DateTimeUtils.atTimeOfDay(start, t)
                        DoseItem(m, at, doses.firstOrNull { it.medicineId == m.id && it.scheduledAt == at }?.givenAt)
                    }
                }
                .sortedBy { it.scheduledAt }
        }
    }
}
