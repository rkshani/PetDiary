package com.diary.petcare.data.reminder

import android.content.Context
import com.diary.petcare.domain.reminder.ReminderActions
import com.diary.petcare.domain.repository.FeedingRepository
import com.diary.petcare.domain.repository.MedicineRepository
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.domain.repository.ReminderRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Workers and receivers are created by the system, not Hilt, so they fetch what they need
 * through this entry point (no extra hilt-work library needed).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun reminders(): ReminderRepository
    fun pets(): PetRepository
    fun medicines(): MedicineRepository
    fun feeding(): FeedingRepository
    fun notifier(): ReminderNotifier
    fun scheduler(): WorkManagerReminderScheduler
    fun actions(): ReminderActions

    companion object {
        fun get(context: Context): ReminderEntryPoint =
            EntryPointAccessors.fromApplication(context.applicationContext, ReminderEntryPoint::class.java)
    }
}
