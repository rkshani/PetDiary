package com.diary.petcare.di

import com.diary.petcare.data.reminder.NoOpReminderScheduler
import com.diary.petcare.domain.reminder.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** v1 binding. Delete this file when the v2 ReminderModule is turned back on. */
@Module
@InstallIn(SingletonComponent::class)
abstract class NoReminderModule {
    @Binds abstract fun scheduler(impl: NoOpReminderScheduler): ReminderScheduler
}
