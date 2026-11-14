package com.diary.petcare.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.Expense
import com.diary.petcare.ui.components.emoji
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.format.LocalAppSettings

/** Monthly total for one pet, with month arrows and a per-category breakdown. */
@Composable
fun MonthlySummaryCard(expenses: List<Expense>, modifier: Modifier = Modifier) {
    val currency = LocalAppSettings.current.currency
    var month by rememberSaveable { mutableLongStateOf(DateTimeUtils.startOfMonth()) }
    val nextMonth = DateTimeUtils.addMonths(month, 1)
    val inMonth = expenses.filter { it.date >= month && it.date < nextMonth }
    val total = inMonth.sumOf { it.amountPaisa }
    val prevMonth = DateTimeUtils.addMonths(month, -1)
    val prevTotal = expenses.filter { it.date >= prevMonth && it.date < month }.sumOf { it.amountPaisa }
    val byCategory = inMonth.groupBy { it.category }
        .mapValues { (_, l) -> l.sumOf { it.amountPaisa } }
        .toList()
        .sortedByDescending { it.second }
    val isCurrent = month >= DateTimeUtils.startOfMonth()

    Card(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { month = prevMonth }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.summary_prev_month))
                }
                Text(
                    Formatters.monthYear(month),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                IconButton(onClick = { month = nextMonth }, enabled = !isCurrent) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.summary_next_month))
                }
            }
            Text(
                Formatters.money(total, currency),
                Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.summary_last_month, Formatters.money(prevTotal, currency)),
                Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
            )
            if (byCategory.isNotEmpty()) {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    byCategory.forEach { (cat, amount) ->
                        CategoryBar(
                            label = "${cat.emoji} ${stringResource(cat.label)}",
                            amount = Formatters.money(amount, currency),
                            fraction = if (total > 0) amount.toFloat() / total else 0f,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBar(label: String, amount: String, fraction: Float) {
    Column {
        Row {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(amount, style = MaterialTheme.typography.labelLarge)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
