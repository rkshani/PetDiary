package com.diary.petcare.ui.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.data.backup.BackupManager
import com.diary.petcare.data.backup.BackupResult
import com.diary.petcare.ui.components.PetTopBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed interface BackupUi {
    data object Idle : BackupUi
    data object Working : BackupUi
    data class BackedUp(val photos: Int) : BackupUi
    data object Restored : BackupUi
    data class Error(val reason: BackupResult.Reason) : BackupUi
}

@HiltViewModel
class BackupViewModel @Inject constructor(private val manager: BackupManager) : ViewModel() {
    private val _ui = MutableStateFlow<BackupUi>(BackupUi.Idle)
    val ui = _ui.asStateFlow()

    fun backup(uri: Uri) {
        _ui.value = BackupUi.Working
        viewModelScope.launch {
            _ui.value = when (val r = manager.backup(uri)) {
                is BackupResult.Done -> BackupUi.BackedUp(r.photos)
                is BackupResult.Failed -> BackupUi.Error(r.reason)
            }
        }
    }

    fun restore(uri: Uri) {
        _ui.value = BackupUi.Working
        viewModelScope.launch {
            _ui.value = when (val r = manager.restore(uri)) {
                is BackupResult.Done -> BackupUi.Restored
                is BackupResult.Failed -> BackupUi.Error(r.reason)
            }
        }
    }

    fun restart() = manager.restartApp()
}

@Composable
fun BackupScreen(onBack: () -> Unit, viewModel: BackupViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var pendingRestore by remember { mutableStateOf<Uri?>(null) }
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) viewModel.backup(uri)
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pendingRestore = uri
    }

    Scaffold(topBar = { PetTopBar(stringResource(R.string.backup_title), onBack = onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.backup_intro), style = MaterialTheme.typography.bodyMedium)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.backup_create_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.backup_create_message), style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = {
                            val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                            create.launch("PetDiary_backup_$stamp.zip")
                        },
                        enabled = ui != BackupUi.Working,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Save, null)
                        Text("  " + stringResource(R.string.backup_create))
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.backup_restore_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.backup_restore_message), style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(
                        onClick = { open.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")) },
                        enabled = ui != BackupUi.Working,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Restore, null)
                        Text("  " + stringResource(R.string.backup_restore))
                    }
                }
            }
            when (val s = ui) {
                BackupUi.Working -> LinearProgressIndicator(Modifier.fillMaxWidth())
                is BackupUi.BackedUp -> Text("✅ " + stringResource(R.string.backup_done, s.photos), color = MaterialTheme.colorScheme.secondary)
                is BackupUi.Error -> Text(
                    stringResource(
                        when (s.reason) {
                            BackupResult.Reason.WRITE -> R.string.backup_error_write
                            BackupResult.Reason.READ -> R.string.backup_error_read
                            BackupResult.Reason.NOT_A_BACKUP -> R.string.backup_error_not_backup
                            BackupResult.Reason.NEWER_VERSION -> R.string.backup_error_newer
                        },
                    ),
                    color = MaterialTheme.colorScheme.error,
                )
                BackupUi.Restored, BackupUi.Idle -> Unit
            }
        }
    }

    pendingRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(stringResource(R.string.backup_restore_confirm_title)) },
            text = { Text(stringResource(R.string.backup_restore_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { pendingRestore = null; viewModel.restore(uri) }) {
                    Text(stringResource(R.string.backup_restore), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
    if (ui == BackupUi.Restored) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.backup_restored_title)) },
            text = { Text(stringResource(R.string.backup_restored_message)) },
            confirmButton = { TextButton(onClick = viewModel::restart) { Text(stringResource(R.string.backup_restart)) } },
        )
    }
}
