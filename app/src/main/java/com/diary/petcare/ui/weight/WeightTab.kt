package com.diary.petcare.ui.weight

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.domain.model.WeightLog
import com.diary.petcare.domain.repository.WeightRepository
import com.diary.petcare.ui.components.EmptyState
import com.diary.petcare.ui.components.LoadingBox
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.format.LocalAppSettings
import com.diary.petcare.ui.health.AddSectionHeader
import com.diary.petcare.ui.health.RecordCard
import com.diary.petcare.ui.navigation.Routes
import com.diary.petcare.ui.profile.ProfileNav
import com.diary.petcare.ui.theme.EventColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class WeightTabViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: WeightRepository,
) : ViewModel() {
    /** Oldest first. Null while loading. */
    val logs: StateFlow<List<WeightLog>?> = repo.observeForPet(savedStateHandle.longArg(Routes.ARG_PET_ID))
        .map<List<WeightLog>, List<WeightLog>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun WeightTab(nav: ProfileNav, viewModel: WeightTabViewModel = hiltViewModel()) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val unit = LocalAppSettings.current.weightUnit
    val list = logs ?: return LoadingBox()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        if (list.size >= 2) item(key = "chart") { WeightChart(list, unit) }
        item { AddSectionHeader(stringResource(R.string.weight_title), onAdd = { nav.editWeight(0) }) }
        if (list.isEmpty()) {
            item {
                EmptyState(
                    emoji = "⚖️",
                    title = stringResource(R.string.weight_empty_title),
                    message = stringResource(R.string.weight_empty_message),
                    actionLabel = stringResource(R.string.weight_add),
                    onAction = { nav.editWeight(0) },
                )
            }
        }
        val newestFirst = list.asReversed()
        itemsIndexed(newestFirst, key = { _, l -> l.id }) { i, log ->
            val prev = newestFirst.getOrNull(i + 1)
            val change = prev?.let { log.weightGrams - it.weightGrams }
            val changeText = change?.takeIf { it != 0L }?.let {
                (if (it > 0) "▲ " else "▼ ") + Formatters.weight(abs(it), unit)
            }
            RecordCard(
                emoji = "⚖️",
                color = EventColors.Weight,
                title = Formatters.weight(log.weightGrams, unit),
                subtitle = listOf(Formatters.date(log.date), log.note).filter { it.isNotBlank() }.joinToString(" • "),
                onClick = { nav.editWeight(log.id) },
                badge = changeText,
                badgeColor = if ((change ?: 0) >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}
