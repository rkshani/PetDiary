package com.diary.petcare.ui.feeding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.domain.model.FeedingSchedule
import com.diary.petcare.domain.repository.FeedingRepository
import com.diary.petcare.domain.usecase.DeleteFeedingScheduleUseCase
import com.diary.petcare.domain.usecase.SaveFeedingScheduleUseCase
import com.diary.petcare.ui.components.EditScaffold
import com.diary.petcare.ui.components.FormTextField
import com.diary.petcare.ui.components.TimeField
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduleForm(
    val timeOfDay: Int = 8 * 60,
    val food: String = "",
    val quantity: String = "",
    val reminder: Boolean = true,
    val foodError: Boolean = false,
    val done: Boolean = false,
)

@HiltViewModel
class FeedingScheduleEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: FeedingRepository,
    private val saveSchedule: SaveFeedingScheduleUseCase,
    private val deleteSchedule: DeleteFeedingScheduleUseCase,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)
    private val id = savedStateHandle.longArg(Routes.ARG_ID)
    val isEdit = id != 0L

    private val _form = MutableStateFlow(ScheduleForm())
    val form = _form.asStateFlow()

    init {
        if (isEdit) viewModelScope.launch {
            repo.getSchedule(id)?.let { s ->
                _form.value = ScheduleForm(s.timeOfDay, s.food, s.quantity, s.reminderEnabled)
            }
        }
    }

    fun update(t: (ScheduleForm) -> ScheduleForm) = _form.update(t)

    fun save() {
        val f = _form.value
        if (f.food.isBlank()) return _form.update { it.copy(foodError = true) }
        viewModelScope.launch {
            saveSchedule(FeedingSchedule(id, petId, f.timeOfDay, f.food.trim(), f.quantity.trim(), f.reminder))
            _form.update { it.copy(done = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deleteSchedule(id)
            _form.update { it.copy(done = true) }
        }
    }
}

@Composable
fun FeedingScheduleEditScreen(onBack: () -> Unit, viewModel: FeedingScheduleEditViewModel = hiltViewModel()) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(form.done) { if (form.done) onBack() }
    EditScaffold(
        title = stringResource(if (viewModel.isEdit) R.string.feeding_edit_time else R.string.feeding_add_time),
        onBack = onBack,
        onSave = viewModel::save,
        onDelete = if (viewModel.isEdit) viewModel::delete else null,
    ) {
        TimeField(
            label = stringResource(R.string.feeding_time),
            minutes = form.timeOfDay,
            onValueChange = { v -> viewModel.update { it.copy(timeOfDay = v) } },
        )
        FormTextField(
            value = form.food,
            onValueChange = { v -> viewModel.update { it.copy(food = v, foodError = false) } },
            label = stringResource(R.string.feeding_food),
            isError = form.foodError,
            supportingText = if (form.foodError) stringResource(R.string.error_required) else null,
        )
        FormTextField(
            value = form.quantity,
            onValueChange = { v -> viewModel.update { it.copy(quantity = v) } },
            label = stringResource(R.string.feeding_quantity),
            supportingText = stringResource(R.string.feeding_quantity_hint),
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.remind_me), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(checked = form.reminder, onCheckedChange = { v -> viewModel.update { it.copy(reminder = v) } })
        }
    }
}
