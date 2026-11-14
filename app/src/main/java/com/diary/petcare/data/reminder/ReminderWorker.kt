package com.diary.petcare.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.ReminderType
import kotlinx.coroutines.flow.first

/**
 * Fires one reminder: shows the notification, then (for repeating reminders) moves the
 * reminder to its next time and schedules itself again.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val deps = ReminderEntryPoint.get(applicationContext)
        val id = inputData.getLong(KEY_ID, 0)
        val reminder = deps.reminders().get(id) ?: return Result.success()
        if (!reminder.enabled) return Result.success()
        val pet = deps.pets().getPet(reminder.petId) ?: return Result.success()
        val now = System.currentTimeMillis()

        // Snooze: show the same occurrence again, do not move the schedule.
        if (inputData.getBoolean(KEY_SNOOZE, false)) {
            deps.notifier().show(reminder, pet.name, inputData.getLong(KEY_OCCURRENCE, reminder.triggerAt))
            return Result.success()
        }

        // The reminder was moved after this job was queued: just follow the new time.
        if (reminder.triggerAt > now + EARLY_SLACK) {
            deps.scheduler().schedule(reminder)
            return Result.success()
        }

        val occurrence = reminder.triggerAt
        // Skip a late medicine dose if the course already ended.
        val courseOver = reminder.type == ReminderType.MEDICINE &&
            deps.medicines().get(reminder.refId)?.endDate?.let { DateTimeUtils.endOfDay(it) < occurrence } == true
        // No need to nag when the meal or dose was already ticked in the app.
        val alreadyDone = when (reminder.type) {
            ReminderType.MEDICINE -> deps.medicines().observeDoses(reminder.refId).first()
                .any { it.scheduledAt == occurrence && it.givenAt != null }
            ReminderType.FEEDING -> deps.feeding()
                .observeLogsBetween(DateTimeUtils.startOfDay(occurrence), DateTimeUtils.endOfDay(occurrence)).first()
                .any { it.scheduleId == reminder.refId }
            else -> false
        }
        if (!courseOver && !alreadyDone) deps.notifier().show(reminder, pet.name, occurrence)

        if (reminder.repeatIntervalMillis > 0) {
            var next = reminder.triggerAt
            while (next <= now) next += reminder.repeatIntervalMillis
            val medicineEnd = if (reminder.type == ReminderType.MEDICINE) deps.medicines().get(reminder.refId)?.endDate else null
            if (medicineEnd != null && next > DateTimeUtils.endOfDay(medicineEnd)) {
                deps.reminders().save(reminder.copy(enabled = false))
            } else {
                val updated = reminder.copy(triggerAt = next)
                deps.reminders().save(updated)
                deps.scheduler().schedule(updated)
            }
        }
        return Result.success()
    }

    companion object {
        const val KEY_ID = "reminder_id"
        const val KEY_SNOOZE = "snooze"
        const val KEY_OCCURRENCE = "occurrence"
        const val KEY_AT = "at"
        private const val EARLY_SLACK = 60_000L
    }
}
