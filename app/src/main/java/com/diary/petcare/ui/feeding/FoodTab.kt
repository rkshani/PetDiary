package com.diary.petcare.ui.feeding

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.petcare.R
import com.diary.petcare.ui.components.EmojiBadge
import com.diary.petcare.ui.components.EmptyState
import com.diary.petcare.ui.components.LoadingBox
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.profile.ProfileNav
import com.diary.petcare.ui.theme.EventColors

@Composable
fun FoodTab(nav: ProfileNav, viewModel: FoodTabViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.loading) return LoadingBox()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            SectionHeader(stringResource(R.string.feeding_schedule)) {
                TextButton(onClick = { nav.editFeedingSchedule(0) }) {
                    Icon(Icons.Filled.Add, null)
                    Text(stringResource(R.string.action_add))
                }
            }
        }
        if (state.schedules.isEmpty()) {
            item {
                EmptyState(
                    emoji = "🍽️",
                    title = stringResource(R.string.feeding_empty_title),
                    message = stringResource(R.string.feeding_empty_message),
                    actionLabel = stringResource(R.string.feeding_add_time),
                    onAction = { nav.editFeedingSchedule(0) },
                )
            }
        }
        items(state.schedules, key = { it.schedule.id }) { row ->
            ScheduleCard(
                row = row,
                onClick = { nav.editFeedingSchedule(row.schedule.id) },
                onFed = { viewModel.markFed(row.schedule) },
                onUndo = { row.fedLog?.let(viewModel::undoFed) },
            )
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = viewModel::logExtraMeal, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.feeding_log_extra))
                }
                OutlinedButton(onClick = nav.feedingHistory, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.History, null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.feeding_history))
                }
            }
        }
        val extras = state.todayLogs.filter { it.scheduleId == null }
        if (extras.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.feeding_extra_today)) }
            items(extras, key = { "log${it.id}" }) { log ->
                Text(
                    "🍖 ${Formatters.time(log.fedAt)}  ${log.food}".trim(),
                    Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ScheduleCard(row: ScheduleRow, onClick: () -> Unit, onFed: () -> Unit, onUndo: () -> Unit) {
    val s = row.schedule
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiBadge("🍽️", EventColors.Feeding)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Formatters.timeOfDay(s.timeOfDay), style = MaterialTheme.typography.titleMedium)
                    if (s.reminderEnabled) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Outlined.NotificationsActive,
                            contentDescription = stringResource(R.string.remind_me),
                            modifier = Modifier.padding(top = 2.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Text(listOf(s.food, s.quantity).filter { it.isNotBlank() }.joinToString(" • "))
            }
            val fed = row.fedLog
            if (fed == null) {
                FilledTonalButton(onClick = onFed) { Text(stringResource(R.string.feeding_fed)) }
            } else {
                TextButton(onClick = onUndo) {
                    Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(4.dp))
                    Text(Formatters.time(fed.fedAt), color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}
