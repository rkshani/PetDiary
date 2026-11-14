package com.diary.petcare.di

import com.diary.petcare.data.reminder.WorkManagerReminderScheduler
import com.diary.petcare.domain.reminder.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {
    @Binds abstract fun scheduler(impl: WorkManagerReminderScheduler): ReminderScheduler
}
