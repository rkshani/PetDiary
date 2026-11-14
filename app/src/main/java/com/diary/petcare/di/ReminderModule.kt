package com.diary.petcare.di

import com.diary.petcare.domain.model.Reminder
import com.diary.petcare.domain.reminder.ReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Placeholder scheduler. The reminders branch swaps this for the WorkManager one. */
@Module
@InstallIn(SingletonComponent::class)
object ReminderModule {
    @Provides
    @Singleton
    fun provideScheduler(): ReminderScheduler = object : ReminderScheduler {
        override fun schedule(reminder: Reminder) = Unit
        override fun cancel(reminderId: Long) = Unit
    }
}
