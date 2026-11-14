package com.diary.petcare

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.diary.petcare.ui.format.LocalAppSettings
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.petcare.data.settings.LocaleStore
import com.diary.petcare.domain.model.ThemeMode
import com.diary.petcare.ui.lock.LockScreen
import com.diary.petcare.ui.navigation.PetDiaryAppUi
import com.diary.petcare.ui.navigation.Routes
import com.diary.petcare.ui.theme.PetDiaryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleStore.wrap(newBase))
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppStarted()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppStopped()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val loaded = settings ?: return@setContent // wait for the first read; the splash covers this
            val dark = when (loaded.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            PetDiaryTheme(darkTheme = dark) {
                CompositionLocalProvider(LocalAppSettings provides loaded) {
                    val unlocked by viewModel.unlocked.collectAsStateWithLifecycle()
                    if (loaded.lockEnabled && !unlocked) {
                        LockScreen(checkPin = viewModel::checkPin, onUnlocked = viewModel::onUnlocked)
                    } else {
                        // Decided once per launch so finishing onboarding does not rebuild the graph.
                        val start = remember { if (loaded.onboardingDone) Routes.HOME else Routes.ONBOARDING }
                        PetDiaryAppUi(startDestination = start)
                    }
                }
            }
        }
    }
}
