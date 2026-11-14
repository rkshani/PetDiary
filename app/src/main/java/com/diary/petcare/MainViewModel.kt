package com.diary.petcare

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.petcare.domain.model.AppSettings
import com.diary.petcare.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val settingsRepository: SettingsRepository) : ViewModel() {
    /** Null until the first settings value is read from disk. */
    val settings: StateFlow<AppSettings?> = settingsRepository.settings
        .map<AppSettings, AppSettings?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** True once the user typed the right PIN in this session. Survives rotation. */
    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    private var stoppedAt = 0L

    init {
        // Turning the lock on inside the app should not lock the user out right away.
        viewModelScope.launch {
            var wasEnabled: Boolean? = null
            settingsRepository.settings.collect { s ->
                if (wasEnabled == false && s.lockEnabled) _unlocked.value = true
                wasEnabled = s.lockEnabled
            }
        }
    }

    suspend fun checkPin(pin: String): Boolean = settingsRepository.checkPin(pin)

    fun onUnlocked() {
        _unlocked.value = true
    }

    fun onAppStopped() {
        stoppedAt = SystemClock.elapsedRealtime()
    }

    /** Locks again after the app has been in the background for a while. */
    fun onAppStarted() {
        if (stoppedAt > 0 && SystemClock.elapsedRealtime() - stoppedAt > RELOCK_AFTER_MS) _unlocked.value = false
        stoppedAt = 0
    }

    private companion object {
        // Long enough to pick a photo or take one with the camera without being locked out.
        const val RELOCK_AFTER_MS = 2 * 60_000L
    }
}
