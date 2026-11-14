package com.diary.petcare.ui.grooming

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.Grooming
import com.diary.petcare.domain.repository.GroomingRepository
import com.diary.petcare.ui.components.emoji
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.format.dueText
import com.diary.petcare.ui.health.AddSectionHeader
import com.diary.petcare.ui.health.RecordCard
import com.diary.petcare.ui.health.SectionEmpty
import com.diary.petcare.ui.navigation.Routes
import com.diary.petcare.ui.profile.ProfileNav
import com.diary.petcare.ui.theme.EventColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GroomingSectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: GroomingRepository,
) : ViewModel() {
    /** Newest first. */
    val items: StateFlow<List<Grooming>> = repo.observeForPet(savedStateHandle.longArg(Routes.ARG_PET_ID))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun GroomingSection(nav: ProfileNav, viewModel: GroomingSectionViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    AddSectionHeader(stringResource(R.string.grooming_title), onAdd = { nav.editGrooming(0) })
    if (items.isEmpty()) SectionEmpty(stringResource(R.string.grooming_empty))
    // Only the newest entry of each type carries the "next due" badge.
    val latestIds = items.groupBy { it.type }.mapValues { (_, l) -> l.maxBy { it.date }.id }.values.toSet()
    items.take(MAX_SHOWN).forEach { g ->
        val next = g.nextDueDate?.takeIf { g.id in latestIds }
        val repeat = if (g.repeatDays > 0) pluralStringResource(R.plurals.grooming_every_days, g.repeatDays, g.repeatDays) else null
        RecordCard(
            emoji = g.type.emoji,
            color = EventColors.Grooming,
            title = stringResource(g.type.label),
            subtitle = listOfNotNull(Formatters.date(g.date), repeat, g.note.ifBlank { null }).joinToString(" • "),
            onClick = { nav.editGrooming(g.id) },
            badge = next?.let { dueText(it) },
            badgeColor = if (next != null && DateTimeUtils.daysFromToday(next) < 0) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
    }
}

private const val MAX_SHOWN = 20
