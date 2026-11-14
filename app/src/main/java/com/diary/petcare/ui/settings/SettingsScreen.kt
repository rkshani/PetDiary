package com.diary.petcare.ui.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.BuildConfig
import com.diary.petcare.R
import com.diary.petcare.core.FeatureFlags
import com.diary.petcare.domain.model.AppLanguage
import com.diary.petcare.domain.model.AppSettings
import com.diary.petcare.domain.model.Currency
import com.diary.petcare.domain.model.ThemeMode
import com.diary.petcare.domain.model.WeightUnit
import com.diary.petcare.domain.repository.SettingsRepository
import com.diary.petcare.ui.components.PetTopBar
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.label
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repo: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repo.setThemeMode(mode) }
    fun setUnit(unit: WeightUnit) = viewModelScope.launch { repo.setWeightUnit(unit) }
    fun setCurrency(currency: Currency) = viewModelScope.launch { repo.setCurrency(currency) }

    /** Saves the language, then [onSaved] recreates the screen in the new language. */
    fun setLanguage(language: AppLanguage, onSaved: () -> Unit) = viewModelScope.launch {
        repo.setLanguage(language)
        onSaved()
    }

    fun disableLock() = viewModelScope.launch { repo.disableLock() }
}

private enum class Picker { THEME, LANGUAGE, UNIT, CURRENCY }

@Composable
fun SettingsScreen(
    onBackup: () -> Unit,
    onSetPin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val s by viewModel.settings.collectAsStateWithLifecycle()
    var picker by remember { mutableStateOf<Picker?>(null) }
    val activity = LocalContext.current as? Activity

    Scaffold(topBar = { PetTopBar(stringResource(R.string.settings_title)) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SectionHeader(stringResource(R.string.settings_general))
            if (FeatureFlags.LANGUAGE_PICKER) {
                SettingRow(Icons.Outlined.Language, stringResource(R.string.settings_language), stringResource(s.language.label)) {
                    picker = Picker.LANGUAGE
                }
            }
            if (FeatureFlags.DARK_MODE) {
                SettingRow(Icons.Outlined.DarkMode, stringResource(R.string.settings_theme), stringResource(s.themeMode.label)) {
                    picker = Picker.THEME
                }
            }
            SettingRow(Icons.Outlined.Scale, stringResource(R.string.settings_weight_unit), stringResource(s.weightUnit.label)) {
                picker = Picker.UNIT
            }
            SettingRow(Icons.Outlined.CurrencyExchange, stringResource(R.string.settings_currency), stringResource(s.currency.label)) {
                picker = Picker.CURRENCY
            }

            if (FeatureFlags.APP_LOCK) {
                SectionHeader(stringResource(R.string.settings_security))
                LockSettings(lockEnabled = s.lockEnabled, onSetPin = onSetPin, onDisable = { viewModel.disableLock() })
            }

            SectionHeader(stringResource(R.string.settings_data))
            SettingRow(Icons.Outlined.Backup, stringResource(R.string.backup_title), stringResource(R.string.settings_backup_hint), onBackup)

            SectionHeader(stringResource(R.string.settings_about))
            SettingRow(
                Icons.Outlined.Info,
                stringResource(R.string.app_name),
                stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
            ) {}
            Text(
                stringResource(R.string.settings_offline_note),
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    when (picker) {
        Picker.THEME -> ChoiceDialog(
            stringResource(R.string.settings_theme), ThemeMode.entries, s.themeMode, { stringResource(it.label) },
            onPick = { viewModel.setTheme(it); picker = null }, onDismiss = { picker = null },
        )
        Picker.LANGUAGE -> ChoiceDialog(
            stringResource(R.string.settings_language), AppLanguage.entries, s.language, { stringResource(it.label) },
            onPick = { lang ->
                picker = null
                if (lang != s.language) viewModel.setLanguage(lang) { activity?.recreate() }
            },
            onDismiss = { picker = null },
        )
        Picker.UNIT -> ChoiceDialog(
            stringResource(R.string.settings_weight_unit), WeightUnit.entries, s.weightUnit, { stringResource(it.label) },
            onPick = { viewModel.setUnit(it); picker = null }, onDismiss = { picker = null },
        )
        Picker.CURRENCY -> ChoiceDialog(
            stringResource(R.string.settings_currency), Currency.entries, s.currency, { stringResource(it.label) },
            onPick = { viewModel.setCurrency(it); picker = null }, onDismiss = { picker = null },
        )
        null -> Unit
    }
}

/** App lock rows (PIN only). */
@Composable
private fun LockSettings(lockEnabled: Boolean, onSetPin: () -> Unit, onDisable: () -> Unit) {
    if (!lockEnabled) {
        SettingRow(Icons.Outlined.Lock, stringResource(R.string.lock_setting), stringResource(R.string.lock_off), onSetPin)
    } else {
        SettingRow(Icons.Outlined.Lock, stringResource(R.string.lock_setting), stringResource(R.string.lock_on)) {}
        SettingRow(Icons.Outlined.Password, stringResource(R.string.lock_change_pin), stringResource(R.string.lock_change_pin_hint), onSetPin)
        SettingRow(Icons.Outlined.LockOpen, stringResource(R.string.lock_turn_off), stringResource(R.string.lock_turn_off_hint), onDisable)
    }
}

@Composable
fun SettingRow(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(value) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick),
    )
    HorizontalDivider(Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
}

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onPick: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { o ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = o == selected, onClick = { onPick(o) }, role = Role.RadioButton)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = o == selected, onClick = null)
                        Text(label(o), Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
