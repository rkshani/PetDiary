package com.diary.petcare.ui.report

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.data.report.HealthReportGenerator
import com.diary.petcare.data.report.ReportOptions
import com.diary.petcare.domain.repository.SettingsRepository
import com.diary.petcare.ui.components.PetTopBar
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ReportState(
    val options: ReportOptions = ReportOptions(),
    val working: Boolean = false,
    val file: File? = null,
    val failed: Boolean = false,
)

@HiltViewModel
class HealthReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val generator: HealthReportGenerator,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)
    private val _state = MutableStateFlow(ReportState())
    val state = _state.asStateFlow()

    fun setOptions(t: (ReportOptions) -> ReportOptions) = _state.update { it.copy(options = t(it.options), file = null) }

    fun generate() {
        _state.update { it.copy(working = true, failed = false, file = null) }
        viewModelScope.launch {
            val file = runCatching { generator.generate(petId, _state.value.options, settings.settings.first()) }.getOrNull()
            _state.update { it.copy(working = false, file = file, failed = file == null) }
        }
    }
}

@Composable
fun HealthReportScreen(onBack: () -> Unit, viewModel: HealthReportViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Scaffold(topBar = { PetTopBar(stringResource(R.string.profile_health_report), onBack = onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.report_intro), style = MaterialTheme.typography.bodyMedium)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    val o = state.options
                    OptionRow(stringResource(R.string.report_profile), o.profile) { v -> viewModel.setOptions { it.copy(profile = v) } }
                    OptionRow(stringResource(R.string.vaccines_title), o.vaccines) { v -> viewModel.setOptions { it.copy(vaccines = v) } }
                    OptionRow(stringResource(R.string.medicines_title), o.medicines) { v -> viewModel.setOptions { it.copy(medicines = v) } }
                    OptionRow(stringResource(R.string.vet_title), o.vetVisits) { v -> viewModel.setOptions { it.copy(vetVisits = v) } }
                    OptionRow(stringResource(R.string.weight_title), o.weight) { v -> viewModel.setOptions { it.copy(weight = v) } }
                }
            }
            Button(onClick = viewModel::generate, enabled = !state.working, modifier = Modifier.fillMaxWidth()) {
                if (state.working) CircularProgressIndicator(Modifier.padding(end = 8.dp)) else Unit
                Text(stringResource(R.string.report_create))
            }
            if (state.failed) Text(stringResource(R.string.report_failed), color = MaterialTheme.colorScheme.error)
            state.file?.let { file ->
                Text("✅ ${stringResource(R.string.report_ready)}", style = MaterialTheme.typography.titleSmall)
                Button(onClick = { sharePdf(context, file) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Share, null)
                    Text("  " + stringResource(R.string.report_share))
                }
                OutlinedButton(onClick = { openPdf(context, file) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, null)
                    Text("  " + stringResource(R.string.report_open))
                }
            }
        }
    }
}

@Composable
private fun OptionRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun uriFor(context: Context, file: File) =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

private fun sharePdf(context: Context, file: File) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uriFor(context, file))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.report_share)))
}

private fun openPdf(context: Context, file: File) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uriFor(context, file), "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        sharePdf(context, file)
    }
}
