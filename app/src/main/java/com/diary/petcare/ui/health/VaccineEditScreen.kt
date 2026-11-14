package com.diary.petcare.ui.health

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.diary.petcare.data.photo.PhotoStorage
import com.diary.petcare.domain.model.Vaccine
import com.diary.petcare.domain.preset.VaccinePresets
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.domain.repository.VaccineRepository
import com.diary.petcare.domain.usecase.DeleteVaccineUseCase
import com.diary.petcare.domain.usecase.SaveVaccineUseCase
import com.diary.petcare.ui.components.DateField
import com.diary.petcare.ui.components.EditScaffold
import com.diary.petcare.ui.components.FormTextField
import com.diary.petcare.ui.components.PhotoBox
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.components.rememberPhotoPicker
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VaccineForm(
    val name: String = "",
    val dateGiven: Long? = DateTimeUtils.startOfDay(),
    val nextDueDate: Long? = null,
    val vetName: String = "",
    val cardPhotoPath: String? = null,
    val notes: String = "",
    val reminder: Boolean = true,
    val suggestions: List<String> = emptyList(),
    val nameError: Boolean = false,
    val done: Boolean = false,
)

@HiltViewModel
class VaccineEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: VaccineRepository,
    private val pets: PetRepository,
    private val saveVaccine: SaveVaccineUseCase,
    private val deleteVaccine: DeleteVaccineUseCase,
    private val photos: PhotoStorage,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)
    private val id = savedStateHandle.longArg(Routes.ARG_ID)
    val isEdit = id != 0L

    private val _form = MutableStateFlow(VaccineForm())
    val form = _form.asStateFlow()
    private var originalPhoto: String? = null

    init {
        viewModelScope.launch {
            val type = pets.getPet(petId)?.type
            val suggestions = type?.let { t -> VaccinePresets.forType(t).map { it.name } }.orEmpty()
            _form.update { it.copy(suggestions = suggestions) }
            if (isEdit) repo.get(id)?.let { v ->
                originalPhoto = v.cardPhotoPath
                _form.update {
                    it.copy(
                        name = v.name, dateGiven = v.dateGiven, nextDueDate = v.nextDueDate, vetName = v.vetName,
                        cardPhotoPath = v.cardPhotoPath, notes = v.notes, reminder = v.reminderEnabled,
                    )
                }
            }
        }
    }

    fun update(t: (VaccineForm) -> VaccineForm) = _form.update(t)

    /** Picks a suggested vaccine and fills in a next due date from its usual interval. */
    fun pickSuggestion(name: String) {
        viewModelScope.launch {
            val type = pets.getPet(petId)?.type ?: return@launch
            val preset = VaccinePresets.forType(type).firstOrNull { it.name == name }
            _form.update {
                val base = it.dateGiven ?: DateTimeUtils.startOfDay()
                it.copy(
                    name = name,
                    nameError = false,
                    nextDueDate = it.nextDueDate ?: preset?.let { p -> DateTimeUtils.addMonths(base, p.repeatMonths) },
                )
            }
        }
    }

    fun newCameraUri(): Uri = photos.newCameraUri()

    fun onPhoto(uri: Uri) {
        viewModelScope.launch {
            photos.import(uri)?.let { p -> _form.update { it.copy(cardPhotoPath = p) } }
        }
    }

    fun save() {
        val f = _form.value
        if (f.name.isBlank()) return _form.update { it.copy(nameError = true) }
        viewModelScope.launch {
            saveVaccine(
                Vaccine(
                    id = id, petId = petId, name = f.name.trim(), dateGiven = f.dateGiven, nextDueDate = f.nextDueDate,
                    vetName = f.vetName.trim(), cardPhotoPath = f.cardPhotoPath, notes = f.notes.trim(),
                    reminderEnabled = f.reminder,
                ),
            )
            if (originalPhoto != null && originalPhoto != f.cardPhotoPath) photos.delete(originalPhoto)
            _form.update { it.copy(done = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deleteVaccine(id)
            photos.delete(originalPhoto)
            _form.update { it.copy(done = true) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaccineEditScreen(onBack: () -> Unit, viewModel: VaccineEditViewModel = hiltViewModel()) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(form.done) { if (form.done) onBack() }
    val picker = rememberPhotoPicker(viewModel::onPhoto, viewModel::newCameraUri)

    EditScaffold(
        title = stringResource(if (viewModel.isEdit) R.string.vaccine_edit else R.string.vaccine_add),
        onBack = onBack,
        onSave = viewModel::save,
        onDelete = if (viewModel.isEdit) viewModel::delete else null,
    ) {
        FormTextField(
            value = form.name,
            onValueChange = { v -> viewModel.update { it.copy(name = v, nameError = false) } },
            label = stringResource(R.string.vaccine_name),
            isError = form.nameError,
            supportingText = if (form.nameError) stringResource(R.string.error_required) else null,
        )
        if (!viewModel.isEdit && form.suggestions.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                form.suggestions.forEach { s ->
                    AssistChip(onClick = { viewModel.pickSuggestion(s) }, label = { Text(s) })
                }
            }
        }
        DateField(
            label = stringResource(R.string.vaccine_date_given),
            value = form.dateGiven,
            onValueChange = { v -> viewModel.update { it.copy(dateGiven = v) } },
            allowClear = true,
        )
        DateField(
            label = stringResource(R.string.vaccine_next_due),
            value = form.nextDueDate,
            onValueChange = { v -> viewModel.update { it.copy(nextDueDate = v) } },
            allowClear = true,
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.remind_me), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(checked = form.reminder, onCheckedChange = { v -> viewModel.update { it.copy(reminder = v) } })
        }
        FormTextField(
            value = form.vetName,
            onValueChange = { v -> viewModel.update { it.copy(vetName = v) } },
            label = stringResource(R.string.vaccine_vet),
        )
        PhotoBox(
            photoPath = form.cardPhotoPath,
            label = stringResource(R.string.vaccine_card_photo),
            picker = picker,
            onRemove = { viewModel.update { it.copy(cardPhotoPath = null) } },
        )
        FormTextField(
            value = form.notes,
            onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
            label = stringResource(R.string.notes),
            singleLine = false,
            minLines = 2,
        )
    }
}
