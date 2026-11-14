package com.diary.petcare.ui.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.diary.petcare.domain.model.Medicine
import com.diary.petcare.domain.model.MedicineKind
import com.diary.petcare.domain.repository.MedicineRepository
import com.diary.petcare.domain.usecase.DeleteMedicineUseCase
import com.diary.petcare.domain.usecase.SaveMedicineUseCase
import com.diary.petcare.ui.components.DateField
import com.diary.petcare.ui.components.DropdownField
import com.diary.petcare.ui.components.EditScaffold
import com.diary.petcare.ui.components.FormTextField
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.TimePickerDialog
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicineForm(
    val name: String = "",
    val kind: MedicineKind = MedicineKind.MEDICINE,
    val dose: String = "",
    val times: List<Int> = listOf(9 * 60),
    val startDate: Long = DateTimeUtils.startOfDay(),
    val endDate: Long? = DateTimeUtils.startOfDay(DateTimeUtils.addDays(DateTimeUtils.now(), 6)),
    val notes: String = "",
    val reminder: Boolean = true,
    val nameError: Boolean = false,
    val dateError: Boolean = false,
    val done: Boolean = false,
)

@HiltViewModel
class MedicineEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: MedicineRepository,
    private val saveMedicine: SaveMedicineUseCase,
    private val deleteMedicine: DeleteMedicineUseCase,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)
    private val id = savedStateHandle.longArg(Routes.ARG_ID)
    val isEdit = id != 0L

    private val _form = MutableStateFlow(MedicineForm())
    val form = _form.asStateFlow()

    init {
        if (isEdit) viewModelScope.launch {
            repo.get(id)?.let { m ->
                _form.value = MedicineForm(
                    name = m.name, kind = m.kind, dose = m.dose, times = m.timesOfDay, startDate = m.startDate,
                    endDate = m.endDate, notes = m.notes, reminder = m.reminderEnabled,
                )
            }
        }
    }

    fun update(t: (MedicineForm) -> MedicineForm) = _form.update(t)

    fun addTime(minutes: Int) = _form.update { it.copy(times = (it.times + minutes).distinct().sorted()) }

    fun removeTime(minutes: Int) = _form.update { it.copy(times = it.times - minutes) }

    /** Quick presets: once, twice or three times a day. */
    fun setTimesPerDay(n: Int) = _form.update {
        it.copy(
            times = when (n) {
                1 -> listOf(9 * 60)
                2 -> listOf(9 * 60, 21 * 60)
                else -> listOf(8 * 60, 14 * 60, 20 * 60)
            },
        )
    }

    fun save() {
        val f = _form.value
        val badDates = f.endDate != null && f.endDate < f.startDate
        if (f.name.isBlank() || badDates) {
            return _form.update { it.copy(nameError = f.name.isBlank(), dateError = badDates) }
        }
        viewModelScope.launch {
            saveMedicine(
                Medicine(
                    id = id, petId = petId, name = f.name.trim(), kind = f.kind, dose = f.dose.trim(),
                    timesOfDay = f.times, startDate = f.startDate, endDate = f.endDate, notes = f.notes.trim(),
                    reminderEnabled = f.reminder,
                ),
            )
            _form.update { it.copy(done = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deleteMedicine(id)
            _form.update { it.copy(done = true) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicineEditScreen(onBack: () -> Unit, viewModel: MedicineEditViewModel = hiltViewModel()) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(form.done) { if (form.done) onBack() }
    var pickTime by rememberSaveable { mutableStateOf(false) }

    EditScaffold(
        title = stringResource(if (viewModel.isEdit) R.string.medicine_edit else R.string.medicine_add),
        onBack = onBack,
        onSave = viewModel::save,
        onDelete = if (viewModel.isEdit) viewModel::delete else null,
    ) {
        FormTextField(
            value = form.name,
            onValueChange = { v -> viewModel.update { it.copy(name = v, nameError = false) } },
            label = stringResource(R.string.medicine_name),
            isError = form.nameError,
            supportingText = if (form.nameError) stringResource(R.string.error_required) else null,
        )
        DropdownField(
            label = stringResource(R.string.medicine_kind),
            options = MedicineKind.entries,
            selected = form.kind,
            optionLabel = { stringResource(it.label) },
            onSelect = { k -> viewModel.update { it.copy(kind = k) } },
        )
        FormTextField(
            value = form.dose,
            onValueChange = { v -> viewModel.update { it.copy(dose = v) } },
            label = stringResource(R.string.medicine_dose),
            supportingText = stringResource(R.string.medicine_dose_hint),
        )

        SectionHeader(stringResource(R.string.medicine_times))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3).forEach { n ->
                AssistChip(
                    onClick = { viewModel.setTimesPerDay(n) },
                    label = { Text(stringResource(R.string.medicine_times_per_day, n)) },
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            form.times.forEach { t ->
                InputChip(
                    selected = true,
                    onClick = { viewModel.removeTime(t) },
                    label = { Text(Formatters.timeOfDay(t)) },
                    trailingIcon = { Icon(Icons.Outlined.Close, stringResource(R.string.action_delete)) },
                )
            }
            AssistChip(
                onClick = { pickTime = true },
                label = { Text(stringResource(R.string.medicine_add_time)) },
                leadingIcon = { Icon(Icons.Filled.Add, null) },
            )
        }

        DateField(
            label = stringResource(R.string.medicine_start),
            value = form.startDate,
            onValueChange = { v -> if (v != null) viewModel.update { it.copy(startDate = v, dateError = false) } },
        )
        DateField(
            label = stringResource(R.string.medicine_end),
            value = form.endDate,
            onValueChange = { v -> viewModel.update { it.copy(endDate = v, dateError = false) } },
            allowClear = true,
        )
        if (form.dateError) {
            Text(stringResource(R.string.medicine_end_before_start), color = MaterialTheme.colorScheme.error)
        } else if (form.endDate == null) {
            Text(
                stringResource(R.string.medicine_no_end),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.remind_me), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(checked = form.reminder, onCheckedChange = { v -> viewModel.update { it.copy(reminder = v) } })
        }
        FormTextField(
            value = form.notes,
            onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
            label = stringResource(R.string.notes),
            singleLine = false,
            minLines = 2,
        )
    }
    if (pickTime) {
        TimePickerDialog(
            initialMinutes = 12 * 60,
            onConfirm = { viewModel.addTime(it); pickTime = false },
            onDismiss = { pickTime = false },
        )
    }
}
