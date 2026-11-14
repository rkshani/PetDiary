package com.diary.petcare.data.reminder

import com.diary.petcare.domain.model.Reminder
import com.diary.petcare.domain.reminder.ReminderScheduler
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Version 1 has no notifications. Reminder rows are still saved in the database, so when
 * the WorkManager scheduler from app/src/v2 comes back, ReminderSync.rescheduleAll() puts
 * them all on the timer.
 */
@Singleton
class NoOpReminderScheduler @Inject constructor() : ReminderScheduler {
    override fun schedule(reminder: Reminder) = Unit
    override fun cancel(reminderId: Long) = Unit
}
