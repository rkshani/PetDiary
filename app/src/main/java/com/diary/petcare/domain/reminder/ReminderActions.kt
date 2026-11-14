package com.diary.petcare.domain.reminder

import com.diary.petcare.domain.model.DAY_MILLIS
import com.diary.petcare.domain.model.MINUTE_MILLIS
import com.diary.petcare.domain.model.Reminder
import com.diary.petcare.domain.model.ReminderType
import com.diary.petcare.domain.repository.FeedingRepository
import com.diary.petcare.domain.repository.ReminderRepository
import com.diary.petcare.domain.usecase.LogFeedingUseCase
import com.diary.petcare.domain.usecase.SetDoseGivenUseCase
import javax.inject.Inject
import javax.inject.Singleton

/** "Mark as done", "Snooze" and "Turn on/off" for reminders, used by notifications and the list. */
@Singleton
class ReminderActions @Inject constructor(
    private val reminders: ReminderRepository,
    private val feeding: FeedingRepository,
    private val logFeeding: LogFeedingUseCase,
    private val setDoseGiven: SetDoseGivenUseCase,
    private val scheduler: ReminderScheduler,
) {
    /**
     * Marks the occurrence at [occurrenceAt] as done. Feeding logs a meal, medicine ticks the
     * dose; one-time reminders (vaccine, vet, grooming) are turned off.
     */
    suspend fun markDone(reminderId: Long, occurrenceAt: Long? = null) {
        val r = reminders.get(reminderId) ?: return
        val now = System.currentTimeMillis()
        val at = occurrenceAt ?: currentOccurrence(r, now)
        when (r.type) {
            ReminderType.FEEDING -> logFeeding(r.petId, feeding.getSchedule(r.refId))
            ReminderType.MEDICINE -> setDoseGiven(r.refId, r.petId, at, true)
            else -> Unit
        }
        if (r.repeatIntervalMillis > 0) {
            // Move past the occurrence that was just handled.
            var next = r.triggerAt
            while (next <= at) next += r.repeatIntervalMillis
            val updated = r.copy(triggerAt = next, lastDoneAt = now)
            reminders.save(updated)
            scheduler.schedule(updated)
        } else {
            reminders.save(r.copy(enabled = false, lastDoneAt = now))
            scheduler.cancel(r.id)
        }
    }

    suspend fun setEnabled(reminderId: Long, enabled: Boolean) {
        val r = reminders.get(reminderId) ?: return
        var updated = r.copy(enabled = enabled)
        if (enabled && r.repeatIntervalMillis > 0) {
            var next = r.triggerAt
            val now = System.currentTimeMillis()
            while (next <= now) next += r.repeatIntervalMillis
            updated = updated.copy(triggerAt = next)
        }
        reminders.save(updated)
        if (enabled) scheduler.schedule(updated) else scheduler.cancel(r.id)
    }

    /** For a repeating reminder, the most recent occurrence that is due (or the next one). */
    private fun currentOccurrence(r: Reminder, now: Long): Long {
        if (r.repeatIntervalMillis <= 0) return r.triggerAt
        val prev = r.triggerAt - r.repeatIntervalMillis
        return if (prev <= now && now - prev < DAY_MILLIS) prev else r.triggerAt
    }

    companion object {
        const val SNOOZE_MINUTES = 15
        val SNOOZE_MILLIS = SNOOZE_MINUTES * MINUTE_MILLIS
    }
}
