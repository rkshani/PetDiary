package com.diary.petcare.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.ui.components.EmptyState
import com.diary.petcare.ui.components.LoadingBox
import com.diary.petcare.ui.components.PetAvatar
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.emoji
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.format.dueText
import com.diary.petcare.ui.health.DoseRow
import com.diary.petcare.ui.health.RecordCard
import com.diary.petcare.ui.theme.EventColors
import kotlinx.coroutines.launch

/** Navigation callbacks used by the home screen. */
data class HomeNav(
    val addPet: () -> Unit,
    val openPet: (Long) -> Unit,
    val logWeight: (Long) -> Unit,
    val addExpense: (Long) -> Unit,
    val addVetVisit: (Long) -> Unit,
    val openVaccine: (petId: Long, id: Long) -> Unit,
    val openVetVisit: (petId: Long, id: Long) -> Unit,
    val openGrooming: (petId: Long, id: Long) -> Unit,
)

@Composable
fun HomeScreen(nav: HomeNav, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val fedMessage = stringResource(R.string.home_fed_logged)

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        if (state.loading) return@Scaffold LoadingBox(Modifier.padding(padding))
        if (state.pets.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                EmptyState(
                    emoji = "🐶",
                    title = stringResource(R.string.home_welcome_title),
                    message = stringResource(R.string.home_welcome_message),
                    actionLabel = stringResource(R.string.pets_add),
                    onAction = nav.addPet,
                )
            }
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
            item { Greeting(state) }
            item {
                SectionHeader(stringResource(R.string.home_quick_log))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.pets, key = { it.id }) { pet ->
                        QuickLogCard(
                            pet = pet,
                            onOpen = { nav.openPet(pet.id) },
                            onFed = {
                                viewModel.quickFed(pet)
                                scope.launch { snackbar.showSnackbar(fedMessage.format(pet.name)) }
                            },
                            onWeight = { nav.logWeight(pet.id) },
                            onExpense = { nav.addExpense(pet.id) },
                            onVet = { nav.addVetVisit(pet.id) },
                        )
                    }
                }
            }

            if (state.feedings.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.home_feeding_today, state.feedingsLeft)) }
                items(state.feedings, key = { "f${it.schedule.id}" }) { f ->
                    RecordCard(
                        emoji = f.pet.type.emoji,
                        color = EventColors.Feeding,
                        title = "${f.pet.name} · ${Formatters.timeOfDay(f.schedule.timeOfDay)}",
                        subtitle = listOf(f.schedule.food, f.schedule.quantity).filter { it.isNotBlank() }.joinToString(" • "),
                        onClick = { nav.openPet(f.pet.id) },
                        trailing = {
                            val log = f.fedLog
                            if (log == null) {
                                FilledTonalButton(onClick = { viewModel.markFed(f) }) { Text(stringResource(R.string.feeding_fed)) }
                            } else {
                                TextButton(onClick = { viewModel.markFed(f) }) {
                                    Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.secondary)
                                    Text(Formatters.time(log.fedAt), color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        },
                    )
                }
            }

            if (state.doses.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.home_medicine_today, state.dosesLeft)) }
                items(state.doses, key = { "d${it.second.medicine.id}_${it.second.scheduledAt}" }) { (pet, dose) ->
                    DoseRow(dose, onToggle = { viewModel.toggleDose(dose) }, petName = pet.name)
                }
            }

            if (state.vaccines.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.home_upcoming_vaccines)) }
                items(state.vaccines, key = { "v${it.second.id}" }) { (pet, v) ->
                    val due = v.nextDueDate ?: 0
                    RecordCard(
                        emoji = "💉",
                        color = EventColors.Vaccine,
                        title = "${pet.name} · ${v.name}",
                        subtitle = Formatters.date(due),
                        onClick = { nav.openVaccine(pet.id, v.id) },
                        badge = dueText(due),
                        badgeColor = if (DateTimeUtils.daysFromToday(due) < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (state.vetVisits.isNotEmpty() || state.groomings.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.home_coming_up)) }
                items(state.vetVisits, key = { "vv${it.second.id}" }) { (pet, v) ->
                    val due = v.nextVisitDate ?: 0
                    RecordCard(
                        emoji = "🩺",
                        color = EventColors.Vet,
                        title = "${pet.name} · ${v.reason.ifBlank { stringResource(R.string.reminder_type_vet) }}",
                        subtitle = Formatters.date(due),
                        onClick = { nav.openVetVisit(pet.id, v.id) },
                        badge = dueText(due),
                    )
                }
                items(state.groomings, key = { "g${it.second.id}" }) { (pet, g) ->
                    val due = g.nextDueDate ?: 0
                    RecordCard(
                        emoji = g.type.emoji,
                        color = EventColors.Grooming,
                        title = "${pet.name} · ${stringResource(g.type.label)}",
                        subtitle = Formatters.date(due),
                        onClick = { nav.openGrooming(pet.id, g.id) },
                        badge = dueText(due),
                        badgeColor = if (DateTimeUtils.daysFromToday(due) < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (state.feedings.isEmpty() && state.doses.isEmpty() && state.vaccines.isEmpty() &&
                state.vetVisits.isEmpty() && state.groomings.isEmpty()
            ) {
                item { EmptyState("😺", stringResource(R.string.home_all_clear), message = stringResource(R.string.home_all_clear_message)) }
            }
        }
    }
}

@Composable
private fun Greeting(state: HomeState) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(Formatters.date(DateTimeUtils.now()), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)
        val left = state.feedingsLeft + state.dosesLeft
        Text(
            if (left == 0) stringResource(R.string.home_nothing_left) else stringResource(R.string.home_tasks_left, left),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuickLogCard(
    pet: Pet,
    onOpen: () -> Unit,
    onFed: () -> Unit,
    onWeight: () -> Unit,
    onExpense: () -> Unit,
    onVet: () -> Unit,
) {
    Card(
        Modifier.width(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.clickable(onClick = onOpen), verticalAlignment = Alignment.CenterVertically) {
                PetAvatar(pet.photoPath, pet.type.emoji, size = 40.dp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(pet.name, style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(pet.type.label), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                QuickButton("🍽️", stringResource(R.string.quick_fed), onFed)
                QuickButton("⚖️", stringResource(R.string.quick_weight), onWeight)
                QuickButton("💰", stringResource(R.string.quick_expense), onExpense)
                QuickButton("🩺", stringResource(R.string.quick_vet), onVet)
            }
        }
    }
}

@Composable
private fun QuickButton(emoji: String, label: String, onClick: () -> Unit) {
    Column(
        Modifier.clickable(onClick = onClick).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
