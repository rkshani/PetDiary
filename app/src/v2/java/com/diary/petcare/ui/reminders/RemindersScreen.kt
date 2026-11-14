package com.diary.petcare.ui.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diary.petcare.R
import com.diary.petcare.domain.model.GroomingType
import com.diary.petcare.domain.model.ReminderType
import com.diary.petcare.ui.components.EmojiBadge
import com.diary.petcare.ui.components.EmptyState
import com.diary.petcare.ui.components.LoadingBox
import com.diary.petcare.ui.components.PetTopBar
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.emoji
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.theme.EventColors
import kotlinx.coroutines.launch

@Composable
fun RemindersScreen(onOpenPet: (Long) -> Unit, viewModel: RemindersViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val doneMsg = stringResource(R.string.reminders_marked_done)
    val snoozeMsg = stringResource(R.string.reminders_snoozed)

    Scaffold(
        topBar = { PetTopBar(stringResource(R.string.reminders_title)) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.loading) return@Scaffold LoadingBox(Modifier.padding(padding))
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
            item { NotificationPermissionBanner() }
            if (state.isEmpty) {
                item {
                    EmptyState(
                        emoji = "🔔",
                        title = stringResource(R.string.reminders_empty_title),
                        message = stringResource(R.string.reminders_empty_message),
                    )
                }
            }
            val row: @Composable (ReminderItem) -> Unit = { item ->
                ReminderRow(
                    item = item,
                    onOpen = { onOpenPet(item.pet.id) },
                    onDone = {
                        viewModel.markDone(item)
                        scope.launch { snackbar.showSnackbar(doneMsg) }
                    },
                    onSnooze = {
                        viewModel.snooze(item)
                        scope.launch { snackbar.showSnackbar(snoozeMsg) }
                    },
                    onToggle = { viewModel.setEnabled(item, it) },
                )
            }
            if (state.today.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.reminders_today)) }
                items(state.today, key = { it.reminder.id }) { row(it) }
            }
            if (state.upcoming.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.reminders_upcoming)) }
                items(state.upcoming, key = { it.reminder.id }) { row(it) }
            }
            if (state.off.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.reminders_off)) }
                items(state.off, key = { it.reminder.id }) { row(it) }
            }
        }
    }
}

@Composable
private fun ReminderRow(
    item: ReminderItem,
    onOpen: () -> Unit,
    onDone: () -> Unit,
    onSnooze: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    val r = item.reminder
    val groomingType = if (r.type == ReminderType.GROOMING) GroomingType.entries.firstOrNull { it.name == r.title } else null
    val subject = groomingType?.let { stringResource(it.label) } ?: r.title
    val color = when (r.type) {
        ReminderType.FEEDING -> EventColors.Feeding
        ReminderType.MEDICINE -> EventColors.Medicine
        ReminderType.VACCINE -> EventColors.Vaccine
        ReminderType.VET -> EventColors.Vet
        ReminderType.GROOMING -> EventColors.Grooming
        ReminderType.CUSTOM -> EventColors.Note
    }
    val whenText = if (r.repeatIntervalMillis > 0) {
        stringResource(R.string.reminders_daily_at, Formatters.time(r.triggerAt))
    } else {
        Formatters.dateTime(r.triggerAt)
    }
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp).clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBadge(r.type.emoji, color)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${item.pet.name} · ${stringResource(r.type.label)}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        listOf(subject, r.message).filter { it.isNotBlank() }.joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(whenText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Switch(checked = r.enabled, onCheckedChange = onToggle)
            }
            if (r.enabled) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onSnooze) { Text(stringResource(R.string.notif_snooze)) }
                    TextButton(onClick = onDone) { Text(stringResource(R.string.notif_done)) }
                }
            }
        }
    }
}

/** Asks for the notification permission on Android 13+ and explains why. */
@Composable
fun NotificationPermissionBanner() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    fun granted() = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    var isGranted by remember { mutableStateOf(granted()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted = it }
    if (isGranted) return
    Card(
        Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.reminders_permission_title), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.reminders_permission_message), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                Text(stringResource(R.string.reminders_permission_allow))
            }
        }
    }
}

/** Asks once for the notification permission when the app first opens (Android 13+). */
@Composable
fun AskNotificationPermissionOnce() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("permission_prompts", android.content.Context.MODE_PRIVATE)
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted && !prefs.getBoolean("notifications_asked", false)) {
            prefs.edit().putBoolean("notifications_asked", true).apply()
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
