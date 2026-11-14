package com.diary.petcare.ui.health

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
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
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.navigation.Routes
import com.diary.petcare.ui.profile.ProfileNav
import com.diary.petcare.ui.theme.EventColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MedicinesSectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: MedicineRepository,
) : ViewModel() {
    val medicines: StateFlow<List<Medicine>> = repo.observeForPet(savedStateHandle.longArg(Routes.ARG_PET_ID))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

fun Medicine.isActiveToday(): Boolean {
    val today = DateTimeUtils.startOfDay()
    return startDate <= DateTimeUtils.endOfDay() && (endDate == null || endDate >= today)
}

val MedicineKind.emoji: String
    get() = when (this) {
        MedicineKind.MEDICINE -> "💊"
        MedicineKind.DEWORMING -> "🪱"
        MedicineKind.FLEA_TICK -> "🐜"
        MedicineKind.SUPPLEMENT -> "🧴"
    }

@Composable
fun MedicinesSection(nav: ProfileNav, viewModel: MedicinesSectionViewModel = hiltViewModel()) {
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    AddSectionHeader(stringResource(R.string.medicines_title), onAdd = { nav.editMedicine(0) })
    if (medicines.isEmpty()) SectionEmpty(stringResource(R.string.medicines_empty))
    medicines.forEach { m ->
        val range = listOfNotNull(
            Formatters.shortDate(m.startDate),
            m.endDate?.let { Formatters.shortDate(it) },
        ).joinToString(" → ")
        val times = m.timesOfDay.joinToString(", ") { Formatters.timeOfDay(it) }
        RecordCard(
            emoji = m.kind.emoji,
            color = EventColors.Medicine,
            title = m.name,
            subtitle = listOf(stringResource(m.kind.label), m.dose, times, range).filter { it.isNotBlank() }.joinToString(" • "),
            onClick = { nav.editMedicine(m.id) },
            badge = if (m.isActiveToday()) stringResource(R.string.medicine_active) else stringResource(R.string.medicine_finished),
        )
    }
}
