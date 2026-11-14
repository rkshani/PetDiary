package com.diary.petcare.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.data.reminder.WorkManagerReminderScheduler
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.domain.model.Reminder
import com.diary.petcare.domain.reminder.ReminderActions
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderItem(val reminder: Reminder, val pet: Pet)

data class RemindersState(
    val loading: Boolean = true,
    val today: List<ReminderItem> = emptyList(),
    val upcoming: List<ReminderItem> = emptyList(),
    val off: List<ReminderItem> = emptyList(),
) {
    val isEmpty get() = today.isEmpty() && upcoming.isEmpty() && off.isEmpty()
}

@HiltViewModel
class RemindersViewModel @Inject constructor(
    reminders: ReminderRepository,
    pets: PetRepository,
    private val actions: ReminderActions,
    private val scheduler: WorkManagerReminderScheduler,
) : ViewModel() {

    val state: StateFlow<RemindersState> = combine(reminders.observeAll(), pets.observePets()) { list, petList ->
        val byId = petList.associateBy { it.id }
        val items = list.mapNotNull { r -> byId[r.petId]?.let { ReminderItem(r, it) } }
        val endToday = DateTimeUtils.endOfDay()
        val (on, off) = items.partition { it.reminder.enabled }
        RemindersState(
            loading = false,
            today = on.filter { it.reminder.triggerAt <= endToday }.sortedBy { it.reminder.triggerAt },
            upcoming = on.filter { it.reminder.triggerAt > endToday }.sortedBy { it.reminder.triggerAt },
            off = off.sortedBy { it.pet.name },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RemindersState())

    fun markDone(item: ReminderItem) {
        viewModelScope.launch { actions.markDone(item.reminder.id) }
    }

    fun snooze(item: ReminderItem) {
        scheduler.snooze(item.reminder.id, item.reminder.triggerAt, ReminderActions.SNOOZE_MINUTES)
    }

    fun setEnabled(item: ReminderItem, enabled: Boolean) {
        viewModelScope.launch { actions.setEnabled(item.reminder.id, enabled) }
    }
}
