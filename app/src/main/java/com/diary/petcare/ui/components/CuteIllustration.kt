package com.diary.petcare.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Small "illustration" for empty states: a soft wobbly blob with paw prints and a
 * gently bobbing emoji. Drawn with Canvas, so it follows the light/dark theme.
 */
@Composable
fun CuteIllustration(emoji: String, modifier: Modifier = Modifier) {
    val blob = MaterialTheme.colorScheme.primaryContainer
    val ring = MaterialTheme.colorScheme.secondaryContainer
    val paw = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "bobY",
    )

    Box(modifier.size(170.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2, size.height / 2)
            val r = size.minDimension * 0.36f
            drawCircle(ring, radius = r * 1.18f, center = c)
            drawPath(blobPath(c, r), blob)
            pawPrint(Offset(size.width * 0.14f, size.height * 0.22f), 9.dp.toPx(), paw)
            pawPrint(Offset(size.width * 0.86f, size.height * 0.30f), 7.dp.toPx(), paw)
            pawPrint(Offset(size.width * 0.80f, size.height * 0.84f), 8.dp.toPx(), paw)
            pawPrint(Offset(size.width * 0.18f, size.height * 0.80f), 6.dp.toPx(), paw)
        }
        Text(emoji, fontSize = 60.sp, modifier = Modifier.graphicsLayer { translationY = bob.dp.toPx() })
    }
}

/** A soft, slightly uneven circle. */
private fun blobPath(c: Offset, r: Float): Path {
    val path = Path()
    val steps = 48
    for (i in 0..steps) {
        val a = (i.toFloat() / steps) * 2 * Math.PI
        val wobble = 1f + 0.05f * sin(3 * a).toFloat() + 0.03f * cos(5 * a).toFloat()
        val x = c.x + r * wobble * cos(a).toFloat()
        val y = c.y + r * wobble * sin(a).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun DrawScope.pawPrint(center: Offset, s: Float, color: Color) {
    drawCircle(color, radius = s, center = center.copy(y = center.y + s * 0.6f))
    val toe = s * 0.42f
    listOf(-1.1f to -0.6f, -0.4f to -1.2f, 0.4f to -1.2f, 1.1f to -0.6f).forEach { (dx, dy) ->
        drawCircle(color, radius = toe, center = Offset(center.x + dx * s, center.y + dy * s))
    }
}
