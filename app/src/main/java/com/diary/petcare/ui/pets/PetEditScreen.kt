package com.diary.petcare.ui.pets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.petcare.R
import com.diary.petcare.domain.model.Gender
import com.diary.petcare.domain.model.PetType
import com.diary.petcare.ui.components.DateField
import com.diary.petcare.ui.components.EditScaffold
import com.diary.petcare.ui.components.FormTextField
import com.diary.petcare.ui.components.LoadingBox
import com.diary.petcare.ui.components.PetAvatar
import com.diary.petcare.ui.components.PhotoSourceMenu
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.emoji
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.components.rememberPhotoPicker

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PetEditScreen(
    onBack: () -> Unit,
    onSaved: (petId: Long, isNew: Boolean) -> Unit,
    onDeleted: () -> Unit,
    viewModel: PetEditViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(form.savedId) { form.savedId?.let { onSaved(it, !viewModel.isEdit) } }
    LaunchedEffect(form.deleted) { if (form.deleted) onDeleted() }

    val picker = rememberPhotoPicker(onPicked = viewModel::onPhotoPicked, cameraUri = viewModel::newCameraUri)
    var photoMenu by remember { mutableStateOf(false) }

    EditScaffold(
        title = stringResource(if (viewModel.isEdit) R.string.pet_edit_title else R.string.pets_add),
        onBack = onBack,
        onSave = viewModel::save,
        onDelete = if (viewModel.isEdit) viewModel::delete else null,
        deleteMessage = stringResource(R.string.pet_delete_message, form.name),
    ) {
        if (!form.loaded) {
            LoadingBox()
            return@EditScaffold
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PetAvatar(
                photoPath = form.photoPath,
                emoji = form.type.emoji,
                size = 112.dp,
                modifier = Modifier.clickable { photoMenu = true },
            )
            PhotoSourceMenu(
                expanded = photoMenu,
                onDismiss = { photoMenu = false },
                picker = picker,
                onRemove = if (form.photoPath != null) viewModel::removePhoto else null,
            )
        }
        Text(
            stringResource(R.string.photo_add),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().clickable { photoMenu = true },
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        FormTextField(
            value = form.name,
            onValueChange = { v -> viewModel.update { it.copy(name = v, nameError = false) } },
            label = stringResource(R.string.pet_name),
            isError = form.nameError,
            supportingText = if (form.nameError) stringResource(R.string.error_required) else null,
        )

        SectionHeader(stringResource(R.string.pet_type), Modifier)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PetType.entries.forEach { t ->
                FilterChip(
                    selected = form.type == t,
                    onClick = { viewModel.update { it.copy(type = t) } },
                    label = { Text("${t.emoji} ${stringResource(t.label)}") },
                )
            }
        }

        FormTextField(
            value = form.breed,
            onValueChange = { v -> viewModel.update { it.copy(breed = v) } },
            label = stringResource(R.string.pet_breed),
        )

        SectionHeader(stringResource(R.string.pet_gender))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            Gender.entries.forEachIndexed { i, g ->
                SegmentedButton(
                    selected = form.gender == g,
                    onClick = { viewModel.update { it.copy(gender = g) } },
                    shape = SegmentedButtonDefaults.itemShape(i, Gender.entries.size),
                ) { Text(stringResource(g.label)) }
            }
        }

        DateField(
            label = stringResource(R.string.pet_birthday),
            value = form.birthDate,
            onValueChange = { v -> viewModel.update { it.copy(birthDate = v, ageYears = "", ageMonths = "") } },
            allowClear = true,
        )
        Text(
            stringResource(R.string.pet_age_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FormTextField(
                value = form.ageYears,
                onValueChange = { viewModel.setAge(it, form.ageMonths) },
                label = stringResource(R.string.pet_age_years),
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            FormTextField(
                value = form.ageMonths,
                onValueChange = { viewModel.setAge(form.ageYears, it) },
                label = stringResource(R.string.pet_age_months),
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }

        FormTextField(
            value = form.color,
            onValueChange = { v -> viewModel.update { it.copy(color = v) } },
            label = stringResource(R.string.pet_color),
        )
        FormTextField(
            value = form.microchip,
            onValueChange = { v -> viewModel.update { it.copy(microchip = v) } },
            label = stringResource(R.string.pet_microchip),
        )
        FormTextField(
            value = form.notes,
            onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
            label = stringResource(R.string.notes),
            singleLine = false,
            minLines = 3,
        )
    }
}
