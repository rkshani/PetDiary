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
import com.diary.petcare.domain.model.AppSettings
import com.diary.petcare.ui.format.LocalAppSettings
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.petcare.data.settings.LocaleStore
import com.diary.petcare.domain.model.ThemeMode
import com.diary.petcare.ui.navigation.PetDiaryAppUi
import com.diary.petcare.ui.theme.PetDiaryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleStore.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val dark = when (settings?.themeMode ?: ThemeMode.SYSTEM) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            PetDiaryTheme(darkTheme = dark) {
                CompositionLocalProvider(LocalAppSettings provides (settings ?: AppSettings())) {
                    PetDiaryAppUi()
                }
            }
        }
    }
}
