package com.diary.petcare.ui.feeding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.FeedingLog
import com.diary.petcare.domain.repository.FeedingRepository
import com.diary.petcare.ui.components.ConfirmDeleteDialog
import com.diary.petcare.ui.components.EmojiBadge
import com.diary.petcare.ui.components.EmptyState
import com.diary.petcare.ui.components.LoadingBox
import com.diary.petcare.ui.components.PetTopBar
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.navigation.Routes
import com.diary.petcare.ui.theme.EventColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Logs grouped by day (local midnight -> logs, newest day first). */
@HiltViewModel
class FeedingHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: FeedingRepository,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)

    val days: StateFlow<List<Pair<Long, List<FeedingLog>>>?> = repo.observeLogs(petId)
        .map { logs -> logs.groupBy { DateTimeUtils.startOfDay(it.fedAt) }.toList().sortedByDescending { it.first } }
        .map<List<Pair<Long, List<FeedingLog>>>, List<Pair<Long, List<FeedingLog>>>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(log: FeedingLog) {
        viewModelScope.launch { repo.deleteLog(log.id) }
    }
}

@Composable
fun FeedingHistoryScreen(onBack: () -> Unit, viewModel: FeedingHistoryViewModel = hiltViewModel()) {
    val days by viewModel.days.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<FeedingLog?>(null) }

    Scaffold(topBar = { PetTopBar(stringResource(R.string.feeding_history_title), onBack = onBack) }) { padding ->
        val list = days
        when {
            list == null -> LoadingBox(Modifier.padding(padding))
            list.isEmpty() -> Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                EmptyState("🥣", stringResource(R.string.feeding_history_empty))
            }
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
                list.forEach { (day, logs) ->
                    item(key = "d$day") {
                        SectionHeader(
                            "${Formatters.date(day)} · " +
                                pluralStringResource(R.plurals.feeding_meals, logs.size, logs.size),
                        )
                    }
                    items(logs, key = { it.id }) { log ->
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            EmojiBadge("🍽️", EventColors.Feeding)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(Formatters.time(log.fedAt), style = MaterialTheme.typography.titleSmall)
                                val detail = listOf(log.food, log.quantity, log.note).filter { it.isNotBlank() }
                                    .joinToString(" • ")
                                    .ifEmpty { stringResource(R.string.feeding_meal) }
                                Text(detail, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { toDelete = log }) {
                                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.action_delete))
                            }
                        }
                    }
                }
            }
        }
    }
    toDelete?.let { log ->
        ConfirmDeleteDialog(
            message = stringResource(R.string.delete_message_generic),
            onConfirm = { viewModel.delete(log) },
            onDismiss = { toDelete = null },
        )
    }
}
