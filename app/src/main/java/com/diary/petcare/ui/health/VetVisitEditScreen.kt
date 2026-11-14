package com.diary.petcare.ui.health

import android.net.Uri
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.data.photo.PhotoStorage
import com.diary.petcare.domain.model.VetVisit
import com.diary.petcare.domain.repository.VetVisitRepository
import com.diary.petcare.domain.usecase.DeleteVetVisitUseCase
import com.diary.petcare.domain.usecase.SaveVetVisitUseCase
import com.diary.petcare.ui.components.DateField
import com.diary.petcare.ui.components.EditScaffold
import com.diary.petcare.ui.components.FormTextField
import com.diary.petcare.ui.components.PhotoBox
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.components.rememberPhotoPicker
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.format.LocalAppSettings
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VetVisitForm(
    val date: Long = DateTimeUtils.startOfDay(),
    val vetName: String = "",
    val clinic: String = "",
    val reason: String = "",
    val diagnosis: String = "",
    val photoPath: String? = null,
    val cost: String = "",
    val nextVisit: Long? = null,
    val reminder: Boolean = true,
    val reasonError: Boolean = false,
    val costError: Boolean = false,
    val done: Boolean = false,
)

@HiltViewModel
class VetVisitEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: VetVisitRepository,
    private val saveVisit: SaveVetVisitUseCase,
    private val deleteVisit: DeleteVetVisitUseCase,
    private val photos: PhotoStorage,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)
    private val id = savedStateHandle.longArg(Routes.ARG_ID)
    val isEdit = id != 0L
    private val _form = MutableStateFlow(VetVisitForm())
    val form = _form.asStateFlow()
    private var originalPhoto: String? = null

    init {
        if (isEdit) viewModelScope.launch {
            repo.get(id)?.let { v ->
                originalPhoto = v.prescriptionPhotoPath
                _form.value = VetVisitForm(
                    date = v.date, vetName = v.vetName, clinic = v.clinic, reason = v.reason, diagnosis = v.diagnosis,
                    photoPath = v.prescriptionPhotoPath, cost = Formatters.moneyInput(v.costPaisa),
                    nextVisit = v.nextVisitDate, reminder = v.reminderEnabled,
                )
            }
        }
    }

    fun update(t: (VetVisitForm) -> VetVisitForm) = _form.update(t)
    fun newCameraUri(): Uri = photos.newCameraUri()
    fun onPhoto(uri: Uri) {
        viewModelScope.launch { photos.import(uri)?.let { p -> _form.update { it.copy(photoPath = p) } } }
    }

    fun save() {
        val f = _form.value
        val cost = if (f.cost.isBlank()) 0L else Formatters.parseMoney(f.cost)
        if (f.reason.isBlank() || cost == null) {
            return _form.update { it.copy(reasonError = f.reason.isBlank(), costError = cost == null) }
        }
        viewModelScope.launch {
            saveVisit(
                VetVisit(
                    id = id, petId = petId, date = f.date, vetName = f.vetName.trim(), clinic = f.clinic.trim(),
                    reason = f.reason.trim(), diagnosis = f.diagnosis.trim(), prescriptionPhotoPath = f.photoPath,
                    costPaisa = cost, nextVisitDate = f.nextVisit, reminderEnabled = f.reminder,
                ),
            )
            if (originalPhoto != null && originalPhoto != f.photoPath) photos.delete(originalPhoto)
            _form.update { it.copy(done = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deleteVisit(id)
            photos.delete(originalPhoto)
            _form.update { it.copy(done = true) }
        }
    }
}

@Composable
fun VetVisitEditScreen(onBack: () -> Unit, viewModel: VetVisitEditViewModel = hiltViewModel()) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(form.done) { if (form.done) onBack() }
    val picker = rememberPhotoPicker(viewModel::onPhoto, viewModel::newCameraUri)
    val currency = LocalAppSettings.current.currency

    EditScaffold(
        title = stringResource(if (viewModel.isEdit) R.string.vet_edit else R.string.vet_add),
        onBack = onBack,
        onSave = viewModel::save,
        onDelete = if (viewModel.isEdit) viewModel::delete else null,
    ) {
        DateField(
            label = stringResource(R.string.vet_date),
            value = form.date,
            onValueChange = { v -> if (v != null) viewModel.update { it.copy(date = v) } },
        )
        FormTextField(
            value = form.reason,
            onValueChange = { v -> viewModel.update { it.copy(reason = v, reasonError = false) } },
            label = stringResource(R.string.vet_reason),
            isError = form.reasonError,
            supportingText = if (form.reasonError) stringResource(R.string.error_required) else null,
        )
        FormTextField(
            value = form.vetName,
            onValueChange = { v -> viewModel.update { it.copy(vetName = v) } },
            label = stringResource(R.string.vet_name),
        )
        FormTextField(
            value = form.clinic,
            onValueChange = { v -> viewModel.update { it.copy(clinic = v) } },
            label = stringResource(R.string.vet_clinic),
        )
        FormTextField(
            value = form.diagnosis,
            onValueChange = { v -> viewModel.update { it.copy(diagnosis = v) } },
            label = stringResource(R.string.vet_diagnosis),
            singleLine = false,
            minLines = 3,
        )
        PhotoBox(
            photoPath = form.photoPath,
            label = stringResource(R.string.vet_prescription_photo),
            picker = picker,
            onRemove = { viewModel.update { it.copy(photoPath = null) } },
        )
        FormTextField(
            value = form.cost,
            onValueChange = { v -> viewModel.update { it.copy(cost = v, costError = false) } },
            label = stringResource(R.string.vet_cost),
            keyboardType = KeyboardType.Decimal,
            suffix = currency.symbol,
            isError = form.costError,
            supportingText = if (form.costError) stringResource(R.string.error_invalid_number) else null,
        )
        DateField(
            label = stringResource(R.string.vet_next_visit),
            value = form.nextVisit,
            onValueChange = { v -> viewModel.update { it.copy(nextVisit = v) } },
            allowClear = true,
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.remind_me), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(checked = form.reminder, onCheckedChange = { v -> viewModel.update { it.copy(reminder = v) } })
        }
    }
}
