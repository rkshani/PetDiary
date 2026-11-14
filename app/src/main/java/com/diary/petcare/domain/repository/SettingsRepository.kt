package com.diary.petcare.domain.repository

import com.diary.petcare.domain.model.AppLanguage
import com.diary.petcare.domain.model.AppSettings
import com.diary.petcare.domain.model.Currency
import com.diary.petcare.domain.model.ThemeMode
import com.diary.petcare.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setWeightUnit(unit: WeightUnit)
    suspend fun setCurrency(currency: Currency)
    suspend fun setOnboardingDone(done: Boolean)

    /** Saves a new PIN (stored only as a salted hash) and turns the lock on. */
    suspend fun setPin(pin: String)
    suspend fun checkPin(pin: String): Boolean
    suspend fun disableLock()
}
