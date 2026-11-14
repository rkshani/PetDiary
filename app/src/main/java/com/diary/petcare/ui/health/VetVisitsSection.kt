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
import com.diary.petcare.domain.model.VetVisit
import com.diary.petcare.domain.repository.VetVisitRepository
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.format.LocalAppSettings
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
class VetVisitsSectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: VetVisitRepository,
) : ViewModel() {
    val visits: StateFlow<List<VetVisit>> = repo.observeForPet(savedStateHandle.longArg(Routes.ARG_PET_ID))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun VetVisitsSection(nav: ProfileNav, viewModel: VetVisitsSectionViewModel = hiltViewModel()) {
    val visits by viewModel.visits.collectAsStateWithLifecycle()
    val currency = LocalAppSettings.current.currency
    AddSectionHeader(stringResource(R.string.vet_title), onAdd = { nav.editVetVisit(0) })
    if (visits.isEmpty()) SectionEmpty(stringResource(R.string.vet_empty))
    visits.forEach { v ->
        val next = v.nextVisitDate?.let { stringResource(R.string.vet_next_on, Formatters.date(it)) }
        RecordCard(
            emoji = "🩺",
            color = EventColors.Vet,
            title = v.reason.ifBlank { stringResource(R.string.reminder_type_vet) },
            subtitle = listOfNotNull(
                Formatters.date(v.date),
                listOf(v.vetName, v.clinic).filter { it.isNotBlank() }.joinToString(", ").ifBlank { null },
                v.costPaisa.takeIf { it > 0 }?.let { Formatters.money(it, currency) },
                next,
            ).joinToString(" • "),
            onClick = { nav.editVetVisit(v.id) },
            badge = v.nextVisitDate?.takeIf { it >= com.diary.petcare.core.util.DateTimeUtils.startOfDay() }?.let { dueText(it) },
        )
    }
}
