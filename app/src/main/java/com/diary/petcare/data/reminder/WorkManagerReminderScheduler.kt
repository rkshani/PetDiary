package com.diary.petcare.data.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.diary.petcare.domain.model.Reminder
import com.diary.petcare.domain.reminder.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Puts each reminder on WorkManager as one unique one-time job. WorkManager keeps the
 * jobs across reboots by itself. Timing is not to-the-second (no exact alarms), which is
 * fine for pet care reminders.
 */
@Singleton
class WorkManagerReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderScheduler {

    private val wm get() = WorkManager.getInstance(context)

    override fun schedule(reminder: Reminder) {
        enqueue(workName(reminder.id), reminder.id, reminder.triggerAt, snooze = false)
    }

    override fun cancel(reminderId: Long) {
        wm.cancelUniqueWork(workName(reminderId))
        wm.cancelUniqueWork(snoozeName(reminderId))
    }

    /** Shows the same occurrence again after [minutes]. */
    fun snooze(reminderId: Long, occurrenceAt: Long, minutes: Int) {
        val at = System.currentTimeMillis() + minutes * 60_000L
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(minutes.toLong(), TimeUnit.MINUTES)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_ID to reminderId,
                    ReminderWorker.KEY_SNOOZE to true,
                    ReminderWorker.KEY_OCCURRENCE to occurrenceAt,
                    ReminderWorker.KEY_AT to at,
                ),
            )
            .addTag(TAG)
            .build()
        wm.enqueueUniqueWork(snoozeName(reminderId), ExistingWorkPolicy.REPLACE, request)
    }

    private fun enqueue(name: String, id: Long, at: Long, snooze: Boolean) {
        val delay = (at - System.currentTimeMillis()).coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_ID to id, ReminderWorker.KEY_SNOOZE to snooze, ReminderWorker.KEY_AT to at))
            .addTag(TAG)
            .build()
        wm.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }

    private fun workName(id: Long) = "reminder_$id"
    private fun snoozeName(id: Long) = "reminder_snooze_$id"

    private companion object {
        const val TAG = "reminders"
    }
}
