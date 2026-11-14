package com.diary.petcare.ui.weight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.WeightLog
import com.diary.petcare.domain.model.WeightUnit
import com.diary.petcare.domain.repository.SettingsRepository
import com.diary.petcare.domain.repository.WeightRepository
import com.diary.petcare.ui.components.DateField
import com.diary.petcare.ui.components.EditScaffold
import com.diary.petcare.ui.components.FormTextField
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

data class WeightForm(
    val date: Long = DateTimeUtils.startOfDay(),
    val weight: String = "",
    val unit: WeightUnit = WeightUnit.KG,
    val note: String = "",
    val weightError: Boolean = false,
    val done: Boolean = false,
)

@HiltViewModel
class WeightEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: WeightRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)
    private val id = savedStateHandle.longArg(Routes.ARG_ID)
    val isEdit = id != 0L
    private val _form = MutableStateFlow(WeightForm())
    val form = _form.asStateFlow()

    init {
        viewModelScope.launch {
            val unit = settings.settings.first().weightUnit
            _form.update { it.copy(unit = unit) }
            if (isEdit) repo.get(id)?.let { w ->
                _form.update {
                    it.copy(date = w.date, weight = Formatters.weightInput(w.weightGrams, unit), note = w.note)
                }
            }
        }
    }

    fun update(t: (WeightForm) -> WeightForm) = _form.update(t)

    fun save() {
        val f = _form.value
        val grams = Formatters.parseWeight(f.weight, f.unit) ?: return _form.update { it.copy(weightError = true) }
        viewModelScope.launch {
            repo.save(WeightLog(id = id, petId = petId, date = f.date, weightGrams = grams, note = f.note.trim()))
            _form.update { it.copy(done = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            repo.delete(id)
            _form.update { it.copy(done = true) }
        }
    }
}

@Composable
fun WeightEditScreen(onBack: () -> Unit, viewModel: WeightEditViewModel = hiltViewModel()) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(form.done) { if (form.done) onBack() }
    EditScaffold(
        title = stringResource(if (viewModel.isEdit) R.string.weight_edit else R.string.weight_add),
        onBack = onBack,
        onSave = viewModel::save,
        onDelete = if (viewModel.isEdit) viewModel::delete else null,
    ) {
        DateField(
            label = stringResource(R.string.date),
            value = form.date,
            onValueChange = { v -> if (v != null) viewModel.update { it.copy(date = v) } },
        )
        FormTextField(
            value = form.weight,
            onValueChange = { v -> viewModel.update { it.copy(weight = v, weightError = false) } },
            label = stringResource(R.string.weight_value),
            keyboardType = KeyboardType.Decimal,
            suffix = if (form.unit == WeightUnit.KG) "kg" else "lb",
            isError = form.weightError,
            supportingText = if (form.weightError) stringResource(R.string.error_invalid_number) else null,
        )
        FormTextField(
            value = form.note,
            onValueChange = { v -> viewModel.update { it.copy(note = v) } },
            label = stringResource(R.string.notes),
        )
    }
}
