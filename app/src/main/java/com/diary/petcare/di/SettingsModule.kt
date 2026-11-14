package com.diary.petcare.di

import com.diary.petcare.data.settings.SettingsRepositoryImpl
import com.diary.petcare.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    @Binds abstract fun settings(impl: SettingsRepositoryImpl): SettingsRepository
}
