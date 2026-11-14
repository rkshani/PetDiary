package com.diary.petcare.ui.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.usecase.ObserveTimelineUseCase
import com.diary.petcare.domain.usecase.TimelineEvent
import com.diary.petcare.domain.usecase.TimelineKind
import com.diary.petcare.ui.components.EmptyState
import com.diary.petcare.ui.components.LoadingBox
import com.diary.petcare.ui.components.PetTopBar
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.emoji
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.format.LocalAppSettings
import com.diary.petcare.ui.health.emoji
import com.diary.petcare.ui.navigation.Routes
import com.diary.petcare.ui.theme.EventColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTimeline: ObserveTimelineUseCase,
) : ViewModel() {
    val events: StateFlow<List<TimelineEvent>?> = observeTimeline(savedStateHandle.longArg(Routes.ARG_PET_ID))
        .map<List<TimelineEvent>, List<TimelineEvent>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun TimelineScreen(onBack: () -> Unit, viewModel: TimelineViewModel = hiltViewModel()) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    Scaffold(topBar = { PetTopBar(stringResource(R.string.profile_timeline), onBack = onBack) }) { padding ->
        val list = events
        when {
            list == null -> LoadingBox(Modifier.padding(padding))
            list.isEmpty() -> Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                EmptyState("📖", stringResource(R.string.timeline_empty))
            }
            else -> {
                val byMonth = list.groupBy { DateTimeUtils.startOfMonth(it.date) }
                LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
                    byMonth.forEach { (month, items) ->
                        item(key = "m$month") { SectionHeader(Formatters.monthYear(month)) }
                        items(items, key = { "${it.kind}_${it.refId}_${it.date}" }) { e ->
                            TimelineRow(e, isLast = e == items.last())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(e: TimelineEvent, isLast: Boolean) {
    val (emoji, color) = e.look()
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = 16.dp)) {
        // Dot and line.
        Box(Modifier.width(28.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2
                if (!isLast) drawLine(lineColor, Offset(cx, 22.dp.toPx()), Offset(cx, size.height), strokeWidth = 2.dp.toPx())
                drawCircle(color, radius = 7.dp.toPx(), center = Offset(cx, 14.dp.toPx()))
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f).padding(bottom = 16.dp)) {
            Text(Formatters.date(e.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$emoji ${e.headline()}", style = MaterialTheme.typography.titleSmall)
            val detail = e.detailText()
            if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodySmall)
            e.photoPath?.let {
                AsyncImage(
                    model = File(it),
                    contentDescription = e.detail.ifBlank { null },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.padding(top = 6.dp).size(120.dp).clip(MaterialTheme.shapes.small),
                )
            }
        }
    }
}

private fun TimelineEvent.look(): Pair<String, Color> = when (kind) {
    TimelineKind.BIRTH -> "🎂" to EventColors.Grooming
    TimelineKind.JOINED -> "🏡" to EventColors.Feeding
    TimelineKind.VACCINE_GIVEN, TimelineKind.VACCINE_DUE -> "💉" to EventColors.Vaccine
    TimelineKind.MEDICINE -> (medicineKind?.emoji ?: "💊") to EventColors.Medicine
    TimelineKind.VET -> "🩺" to EventColors.Vet
    TimelineKind.WEIGHT -> "⚖️" to EventColors.Weight
    TimelineKind.GROOMING -> (groomingType?.emoji ?: "🛁") to EventColors.Grooming
    TimelineKind.EXPENSE -> (expenseCategory?.emoji ?: "💰") to EventColors.Expense
    TimelineKind.NOTE -> "📝" to EventColors.Note
    TimelineKind.PHOTO -> "📸" to EventColors.Note
}

@Composable
private fun TimelineEvent.headline(): String {
    val settings = LocalAppSettings.current
    return when (kind) {
        TimelineKind.BIRTH -> stringResource(R.string.timeline_born, title)
        TimelineKind.JOINED -> stringResource(R.string.timeline_joined, title)
        TimelineKind.VACCINE_GIVEN -> stringResource(R.string.timeline_vaccine_given, title)
        TimelineKind.VACCINE_DUE -> stringResource(R.string.timeline_vaccine_due, title)
        TimelineKind.MEDICINE -> stringResource(R.string.timeline_medicine, title)
        TimelineKind.VET -> stringResource(R.string.timeline_vet, title.ifBlank { stringResource(R.string.reminder_type_vet) })
        TimelineKind.WEIGHT -> stringResource(R.string.timeline_weight, Formatters.weight(weightGrams ?: 0, settings.weightUnit))
        TimelineKind.GROOMING -> groomingType?.let { stringResource(it.label) } ?: stringResource(R.string.reminder_type_grooming)
        TimelineKind.EXPENSE -> (expenseCategory?.let { stringResource(it.label) } ?: "") + " · " +
            Formatters.money(amountPaisa ?: 0, settings.currency)
        TimelineKind.NOTE -> title
        TimelineKind.PHOTO -> stringResource(R.string.timeline_photo)
    }
}

@Composable
private fun TimelineEvent.detailText(): String {
    val currency = LocalAppSettings.current.currency
    return when (kind) {
        TimelineKind.VET -> listOfNotNull(detail.ifBlank { null }, amountPaisa?.let { Formatters.money(it, currency) }).joinToString(" • ")
        TimelineKind.JOINED, TimelineKind.BIRTH -> ""
        else -> detail
    }
}

