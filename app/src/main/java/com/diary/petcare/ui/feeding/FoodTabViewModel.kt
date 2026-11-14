package com.diary.petcare.ui.feeding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.FeedingLog
import com.diary.petcare.domain.model.FeedingSchedule
import com.diary.petcare.domain.repository.FeedingRepository
import com.diary.petcare.domain.usecase.LogFeedingUseCase
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduleRow(val schedule: FeedingSchedule, val fedLog: FeedingLog?)

data class FoodTabState(
    val loading: Boolean = true,
    val schedules: List<ScheduleRow> = emptyList(),
    val todayLogs: List<FeedingLog> = emptyList(),
)

@HiltViewModel
class FoodTabViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: FeedingRepository,
    private val logFeeding: LogFeedingUseCase,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)

    val state: StateFlow<FoodTabState> = combine(
        repo.observeSchedules(petId),
        repo.observeLogsBetween(DateTimeUtils.startOfDay(), DateTimeUtils.endOfDay())
            .map { logs -> logs.filter { it.petId == petId } },
    ) { schedules, logs ->
        FoodTabState(
            loading = false,
            schedules = schedules.map { s -> ScheduleRow(s, logs.firstOrNull { it.scheduleId == s.id }) },
            todayLogs = logs,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoodTabState())

    fun markFed(schedule: FeedingSchedule) {
        viewModelScope.launch { logFeeding(petId, schedule) }
    }

    fun undoFed(log: FeedingLog) {
        viewModelScope.launch { repo.deleteLog(log.id) }
    }

    fun logExtraMeal() {
        viewModelScope.launch { logFeeding(petId) }
    }
}
