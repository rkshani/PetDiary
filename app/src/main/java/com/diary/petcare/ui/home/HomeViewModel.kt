package com.diary.petcare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.DAY_MILLIS
import com.diary.petcare.domain.model.FeedingLog
import com.diary.petcare.domain.model.FeedingSchedule
import com.diary.petcare.domain.model.Grooming
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.domain.model.Vaccine
import com.diary.petcare.domain.model.VetVisit
import com.diary.petcare.domain.repository.FeedingRepository
import com.diary.petcare.domain.repository.GroomingRepository
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.domain.repository.VaccineRepository
import com.diary.petcare.domain.repository.VetVisitRepository
import com.diary.petcare.domain.usecase.DoseItem
import com.diary.petcare.domain.usecase.LogFeedingUseCase
import com.diary.petcare.domain.usecase.ObserveDosesUseCase
import com.diary.petcare.domain.usecase.SetDoseGivenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedingDue(val pet: Pet, val schedule: FeedingSchedule, val fedLog: FeedingLog?)

data class HomeState(
    val loading: Boolean = true,
    val pets: List<Pet> = emptyList(),
    val feedings: List<FeedingDue> = emptyList(),
    val doses: List<Pair<Pet, DoseItem>> = emptyList(),
    val vaccines: List<Pair<Pet, Vaccine>> = emptyList(),
    val vetVisits: List<Pair<Pet, VetVisit>> = emptyList(),
    val groomings: List<Pair<Pet, Grooming>> = emptyList(),
    /** Snack bar message key after a quick log. */
    val loggedPetName: String? = null,
) {
    val feedingsLeft get() = feedings.count { it.fedLog == null }
    val dosesLeft get() = doses.count { !it.second.given }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    pets: PetRepository,
    private val feeding: FeedingRepository,
    vaccines: VaccineRepository,
    vetVisits: VetVisitRepository,
    groomings: GroomingRepository,
    observeDoses: ObserveDosesUseCase,
    private val logFeeding: LogFeedingUseCase,
    private val setDoseGiven: SetDoseGivenUseCase,
) : ViewModel() {

    private val todayStart = DateTimeUtils.startOfDay()
    private val todayEnd = DateTimeUtils.endOfDay()
    private val soon = todayEnd + UPCOMING_DAYS * DAY_MILLIS

    private val feedingFlow = combine(
        feeding.observeAllSchedules(),
        feeding.observeLogsBetween(todayStart, todayEnd),
    ) { s, l -> s to l }

    private val upcomingFlow = combine(
        vaccines.observeDueBefore(soon),
        vetVisits.observeNextVisitsBefore(soon),
        groomings.observeAll(),
    ) { v, vv, g -> Triple(v, vv, g) }

    val state: StateFlow<HomeState> = combine(
        pets.observePets(),
        feedingFlow,
        observeDoses(),
        upcomingFlow,
    ) { petList, (schedules, logs), doses, (vacs, visits, grooms) ->
        val byId = petList.associateBy { it.id }
        HomeState(
            loading = false,
            pets = petList,
            feedings = schedules.mapNotNull { s ->
                byId[s.petId]?.let { p -> FeedingDue(p, s, logs.firstOrNull { it.scheduleId == s.id }) }
            }.sortedBy { it.schedule.timeOfDay },
            doses = doses.mapNotNull { d -> byId[d.medicine.petId]?.let { it to d } },
            vaccines = vacs.mapNotNull { v -> byId[v.petId]?.let { it to v } },
            vetVisits = visits.filter { (it.nextVisitDate ?: 0) >= todayStart }
                .mapNotNull { v -> byId[v.petId]?.let { it to v } },
            groomings = latestGroomingDue(grooms).mapNotNull { g -> byId[g.petId]?.let { it to g } },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    /** Newest entry per pet and type whose next due date is within the window. */
    private fun latestGroomingDue(all: List<Grooming>): List<Grooming> =
        all.groupBy { it.petId to it.type }
            .mapNotNull { (_, l) -> l.maxBy { it.date } }
            .filter { g -> g.nextDueDate?.let { it <= soon } == true }
            .sortedBy { it.nextDueDate }

    fun markFed(item: FeedingDue) {
        viewModelScope.launch {
            val log = item.fedLog
            if (log == null) logFeeding(item.pet.id, item.schedule) else feeding.deleteLog(log.id)
        }
    }

    fun toggleDose(item: DoseItem) {
        viewModelScope.launch { setDoseGiven(item.medicine.id, item.medicine.petId, item.scheduledAt, !item.given) }
    }

    /** Quick "fed" button on a pet chip: logs a meal now. */
    fun quickFed(pet: Pet) {
        viewModelScope.launch { logFeeding(pet.id) }
    }

    private companion object {
        const val UPCOMING_DAYS = 30
    }
}
