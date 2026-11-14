package com.diary.petcare.ui.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.diary.petcare.R
import com.diary.petcare.domain.model.WeightLog
import com.diary.petcare.domain.model.WeightUnit
import com.diary.petcare.ui.format.Formatters
import java.text.DecimalFormat

/**
 * Simple growth chart drawn with Canvas. X is time (spaced by real dates), Y is weight
 * in the chosen unit. Needs at least two records to draw a line.
 */
@Composable
fun WeightChart(logs: List<WeightLog>, unit: WeightUnit, modifier: Modifier = Modifier) {
    if (logs.size < 2) return
    val line = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val measurer = rememberTextMeasurer()
    val df = remember { DecimalFormat("0.#") }

    val points = logs.sortedBy { it.date }
    val values = points.map { Formatters.weightValue(it.weightGrams, unit) }
    val rawMin = values.min()
    val rawMax = values.max()
    val pad = ((rawMax - rawMin) * 0.15).coerceAtLeast(0.1)
    val minY = (rawMin - pad).coerceAtLeast(0.0)
    val maxY = rawMax + pad
    val minX = points.first().date
    val maxX = points.last().date.coerceAtLeast(minX + 1)
    val unitLabel = if (unit == WeightUnit.KG) "kg" else "lb"
    val summary = stringResource(
        R.string.weight_chart_summary,
        Formatters.weight(points.first().weightGrams, unit),
        Formatters.weight(points.last().weightGrams, unit),
    )

    Card(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(stringResource(R.string.weight_chart_title), style = MaterialTheme.typography.titleSmall)
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp)
                    .semantics { contentDescription = summary },
            ) {
                val leftPad = 40.dp.toPx()
                val bottomPad = 20.dp.toPx()
                val w = size.width - leftPad
                val h = size.height - bottomPad

                fun x(date: Long) = leftPad + (date - minX).toFloat() / (maxX - minX) * w
                fun y(v: Double) = (h - (v - minY) / (maxY - minY) * h).toFloat()

                // Grid lines with value labels.
                val steps = 3
                for (i in 0..steps) {
                    val v = minY + (maxY - minY) * i / steps
                    val yy = y(v)
                    drawLine(
                        grid, Offset(leftPad, yy), Offset(size.width, yy), strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                    )
                    val text = measurer.measure("${df.format(v)}", labelStyle)
                    drawText(text, topLeft = Offset(0f, yy - text.size.height / 2f))
                }
                // Unit and first/last date labels.
                drawText(measurer.measure(unitLabel, labelStyle), topLeft = Offset(0f, h + 2.dp.toPx()))
                val first = measurer.measure(Formatters.shortDate(minX), labelStyle)
                drawText(first, topLeft = Offset(leftPad, h + 2.dp.toPx()))
                val last = measurer.measure(Formatters.shortDate(points.last().date), labelStyle)
                drawText(last, topLeft = Offset(size.width - last.size.width, h + 2.dp.toPx()))

                // Area under the line, then the line and dots.
                val path = Path()
                val area = Path()
                points.forEachIndexed { i, p ->
                    val px = x(p.date)
                    val py = y(values[i])
                    if (i == 0) {
                        path.moveTo(px, py)
                        area.moveTo(px, h)
                        area.lineTo(px, py)
                    } else {
                        path.lineTo(px, py)
                        area.lineTo(px, py)
                    }
                }
                area.lineTo(x(points.last().date), h)
                area.close()
                drawPath(area, Brush.verticalGradient(listOf(line.copy(alpha = 0.25f), line.copy(alpha = 0f)), 0f, h))
                drawPath(path, line, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                points.forEachIndexed { i, p ->
                    drawCircle(line, radius = 4.dp.toPx(), center = Offset(x(p.date), y(values[i])))
                }
            }
        }
    }
}
