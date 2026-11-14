package com.diary.petcare.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diary.petcare.R
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.format.Formatters

/** Overview: pet details. Later branches add grooming and notes sections below. */
@Composable
fun OverviewTab(pet: Pet, nav: ProfileNav, extraSections: @Composable () -> Unit = {}) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        SectionHeader(stringResource(R.string.overview_details))
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(Modifier.padding(vertical = 8.dp)) {
                DetailRow(stringResource(R.string.pet_type), stringResource(pet.type.label))
                DetailRow(stringResource(R.string.pet_breed), pet.breed)
                DetailRow(stringResource(R.string.pet_gender), stringResource(pet.gender.label))
                DetailRow(stringResource(R.string.pet_birthday), pet.birthDate?.let { Formatters.date(it) }.orEmpty())
                DetailRow(stringResource(R.string.pet_color), pet.color)
                DetailRow(stringResource(R.string.pet_microchip), pet.microchip)
            }
        }
        if (pet.notes.isNotBlank()) {
            SectionHeader(stringResource(R.string.notes))
            Text(pet.notes, Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyMedium)
        }
        extraSections()
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
            label,
            Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, Modifier.weight(1.4f), style = MaterialTheme.typography.bodyLarge)
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
}
