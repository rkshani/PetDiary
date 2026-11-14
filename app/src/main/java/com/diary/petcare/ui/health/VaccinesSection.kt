package com.diary.petcare.ui.health

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.diary.petcare.domain.model.Vaccine
import com.diary.petcare.domain.repository.VaccineRepository
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.format.dueText
import com.diary.petcare.ui.navigation.Routes
import com.diary.petcare.ui.profile.ProfileNav
import com.diary.petcare.ui.theme.EventColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VaccinesSectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: VaccineRepository,
) : ViewModel() {
    val vaccines: StateFlow<List<Vaccine>> = repo.observeForPet(savedStateHandle.longArg(Routes.ARG_PET_ID))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun VaccinesSection(nav: ProfileNav, viewModel: VaccinesSectionViewModel = hiltViewModel()) {
    val vaccines by viewModel.vaccines.collectAsStateWithLifecycle()
    AddSectionHeader(
        title = stringResource(R.string.vaccines_title),
        onAdd = { nav.editVaccine(0) },
        extra = { TextButton(onClick = nav.vaccinePresets) { Text(stringResource(R.string.vaccines_presets)) } },
    )
    if (vaccines.isEmpty()) SectionEmpty(stringResource(R.string.vaccines_empty))
    vaccines.forEach { v -> VaccineRow(v) { nav.editVaccine(v.id) } }
}

@Composable
private fun VaccineRow(v: Vaccine, onClick: () -> Unit) {
    val given = v.dateGiven?.let { stringResource(R.string.vaccine_given_on, Formatters.date(it)) }
    val next = v.nextDueDate?.let { stringResource(R.string.vaccine_next_on, Formatters.date(it)) }
    val due = v.nextDueDate
    val overdue = due != null && DateTimeUtils.daysFromToday(due) < 0
    RecordCard(
        emoji = "💉",
        color = EventColors.Vaccine,
        title = v.name,
        subtitle = listOfNotNull(given, next, v.vetName.takeIf { it.isNotBlank() }).joinToString("\n"),
        onClick = onClick,
        badge = due?.let { dueText(it) },
        badgeColor = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
    )
}
