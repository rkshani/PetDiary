package com.diary.petcare.ui.grooming

import com.diary.petcare.core.FeatureFlags
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.Grooming
import com.diary.petcare.domain.model.GroomingType
import com.diary.petcare.domain.repository.GroomingRepository
import com.diary.petcare.domain.usecase.DeleteGroomingUseCase
import com.diary.petcare.domain.usecase.SaveGroomingUseCase
import com.diary.petcare.ui.components.DateField
import com.diary.petcare.ui.components.EditScaffold
import com.diary.petcare.ui.components.FormTextField
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.emoji
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroomingForm(
    val type: GroomingType = GroomingType.BATH,
    val date: Long = DateTimeUtils.startOfDay(),
    val repeatDays: String = "",
    val note: String = "",
    val reminder: Boolean = true,
    val done: Boolean = false,
)

/** Usual repeat gaps (days) offered as quick picks per type. */
private fun GroomingType.defaultRepeat(): Int = when (this) {
    GroomingType.BATH -> 30
    GroomingType.NAIL_CUT -> 21
    GroomingType.HAIRCUT -> 60
    GroomingType.BRUSHING -> 7
    GroomingType.TEETH -> 7
    GroomingType.EAR_CLEAN -> 14
    GroomingType.OTHER -> 0
}

@HiltViewModel
class GroomingEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: GroomingRepository,
    private val saveGrooming: SaveGroomingUseCase,
    private val deleteGrooming: DeleteGroomingUseCase,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)
    private val id = savedStateHandle.longArg(Routes.ARG_ID)
    val isEdit = id != 0L
    private val _form = MutableStateFlow(GroomingForm(repeatDays = GroomingType.BATH.defaultRepeat().toString()))
    val form = _form.asStateFlow()

    init {
        if (isEdit) viewModelScope.launch {
            repo.get(id)?.let { g ->
                _form.value = GroomingForm(
                    type = g.type, date = g.date, repeatDays = if (g.repeatDays > 0) g.repeatDays.toString() else "",
                    note = g.note, reminder = g.reminderEnabled,
                )
            }
        }
    }

    fun update(t: (GroomingForm) -> GroomingForm) = _form.update(t)

    fun setType(type: GroomingType) = _form.update {
        it.copy(type = type, repeatDays = if (isEdit) it.repeatDays else type.defaultRepeat().takeIf { d -> d > 0 }?.toString().orEmpty())
    }

    fun save() {
        val f = _form.value
        viewModelScope.launch {
            val older = repo.observeForPet(petId).first()
            saveGrooming(
                Grooming(
                    id = id, petId = petId, type = f.type, date = f.date,
                    repeatDays = f.repeatDays.toIntOrNull()?.coerceIn(0, MAX_REPEAT) ?: 0,
                    note = f.note.trim(), reminderEnabled = f.reminder,
                ),
                older,
            )
            _form.update { it.copy(done = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deleteGrooming(id)
            _form.update { it.copy(done = true) }
        }
    }

    private companion object {
        const val MAX_REPEAT = 365
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroomingEditScreen(onBack: () -> Unit, viewModel: GroomingEditViewModel = hiltViewModel()) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(form.done) { if (form.done) onBack() }

    EditScaffold(
        title = stringResource(if (viewModel.isEdit) R.string.grooming_edit else R.string.grooming_add),
        onBack = onBack,
        onSave = viewModel::save,
        onDelete = if (viewModel.isEdit) viewModel::delete else null,
    ) {
        SectionHeader(stringResource(R.string.grooming_what))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GroomingType.entries.forEach { t ->
                FilterChip(
                    selected = form.type == t,
                    onClick = { viewModel.setType(t) },
                    label = { Text("${t.emoji} ${stringResource(t.label)}") },
                )
            }
        }
        DateField(
            label = stringResource(R.string.date),
            value = form.date,
            onValueChange = { v -> if (v != null) viewModel.update { it.copy(date = v) } },
        )
        SectionHeader(stringResource(R.string.grooming_repeat))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0, 7, 14, 30, 60, 90).forEach { d ->
                FilterChip(
                    selected = (form.repeatDays.toIntOrNull() ?: 0) == d,
                    onClick = { viewModel.update { it.copy(repeatDays = if (d == 0) "" else d.toString()) } },
                    label = {
                        Text(
                            if (d == 0) {
                                stringResource(R.string.grooming_no_repeat)
                            } else {
                                pluralStringResource(R.plurals.grooming_every_days, d, d)
                            },
                        )
                    },
                )
            }
        }
        FormTextField(
            value = form.repeatDays,
            onValueChange = { v -> viewModel.update { it.copy(repeatDays = v.filter(Char::isDigit).take(3)) } },
            label = stringResource(R.string.grooming_repeat_days),
            keyboardType = KeyboardType.Number,
        )
        val days = form.repeatDays.toIntOrNull() ?: 0
        if (days > 0) {
            Text(
                stringResource(R.string.grooming_next_due, Formatters.date(DateTimeUtils.addDays(form.date, days))),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (FeatureFlags.REMINDERS) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.remind_me), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = form.reminder, onCheckedChange = { v -> viewModel.update { it.copy(reminder = v) } })
                }
            }
        }
        FormTextField(
            value = form.note,
            onValueChange = { v -> viewModel.update { it.copy(note = v) } },
            label = stringResource(R.string.notes),
        )
    }
}
