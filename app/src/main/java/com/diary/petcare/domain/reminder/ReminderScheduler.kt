package com.diary.petcare.domain.reminder

import com.diary.petcare.domain.model.Reminder

/** Puts a reminder on the system timer (WorkManager) or takes it off. */
interface ReminderScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminderId: Long)
}
