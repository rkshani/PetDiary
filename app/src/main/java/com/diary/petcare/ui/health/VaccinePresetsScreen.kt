package com.diary.petcare.ui.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.Vaccine
import com.diary.petcare.domain.preset.VaccinePresets
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.domain.repository.VaccineRepository
import com.diary.petcare.domain.usecase.SaveVaccineUseCase
import com.diary.petcare.ui.components.DateField
import com.diary.petcare.ui.components.EditScaffold
import com.diary.petcare.ui.components.FormTextField
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PresetRow(
    val key: Int,
    val name: String,
    val dueDate: Long?,
    val checked: Boolean = true,
    /** Already in the pet's records, so unchecked by default. */
    val existing: Boolean = false,
)

data class PresetsState(val rows: List<PresetRow> = emptyList(), val done: Boolean = false)

@HiltViewModel
class VaccinePresetsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pets: PetRepository,
    private val vaccines: VaccineRepository,
    private val saveVaccine: SaveVaccineUseCase,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)
    private val _state = MutableStateFlow(PresetsState())
    val state = _state.asStateFlow()
    private var nextKey = 0

    init {
        viewModelScope.launch {
            val type = pets.getPet(petId)?.type ?: return@launch
            val have = vaccines.getForPet(petId).map { it.name.lowercase() }.toSet()
            val today = DateTimeUtils.startOfDay()
            _state.value = PresetsState(
                VaccinePresets.forType(type).map { p ->
                    val exists = p.name.lowercase() in have
                    PresetRow(nextKey++, p.name, today, checked = !exists, existing = exists)
                },
            )
        }
    }

    fun edit(key: Int, t: (PresetRow) -> PresetRow) =
        _state.update { s -> s.copy(rows = s.rows.map { if (it.key == key) t(it) else it }) }

    fun addRow() = _state.update { it.copy(rows = it.rows + PresetRow(nextKey++, "", DateTimeUtils.startOfDay())) }

    fun removeRow(key: Int) = _state.update { s -> s.copy(rows = s.rows.filterNot { it.key == key }) }

    fun addSelected() {
        viewModelScope.launch {
            _state.value.rows.filter { it.checked && it.name.isNotBlank() }.forEach { r ->
                saveVaccine(Vaccine(petId = petId, name = r.name.trim(), dateGiven = null, nextDueDate = r.dueDate))
            }
            _state.update { it.copy(done = true) }
        }
    }
}

@Composable
fun VaccinePresetsScreen(onBack: () -> Unit, viewModel: VaccinePresetsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) { if (state.done) onBack() }

    EditScaffold(
        title = stringResource(R.string.vaccines_presets),
        onBack = onBack,
        onSave = viewModel::addSelected,
        saveEnabled = state.rows.any { it.checked && it.name.isNotBlank() },
    ) {
        Text(
            stringResource(R.string.presets_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state.rows.isEmpty()) {
            SectionEmpty(stringResource(R.string.presets_none_for_type))
        }
        state.rows.forEach { row ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = row.checked,
                            onCheckedChange = { c -> viewModel.edit(row.key) { it.copy(checked = c) } },
                        )
                        FormTextField(
                            value = row.name,
                            onValueChange = { v -> viewModel.edit(row.key) { it.copy(name = v) } },
                            label = stringResource(R.string.vaccine_name),
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.removeRow(row.key) }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                    if (row.existing) {
                        Text(
                            stringResource(R.string.presets_already_added),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    if (row.checked) {
                        DateField(
                            label = stringResource(R.string.vaccine_next_due),
                            value = row.dueDate,
                            onValueChange = { d -> viewModel.edit(row.key) { it.copy(dueDate = d) } },
                            allowClear = true,
                        )
                    }
                }
            }
        }
        OutlinedButton(onClick = viewModel::addRow, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, null)
            Text(stringResource(R.string.presets_add_custom))
        }
    }
}
