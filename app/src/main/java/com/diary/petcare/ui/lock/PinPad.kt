package com.diary.petcare.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diary.petcare.R

const val PIN_MIN = 4
const val PIN_MAX = 6

/** Full-screen PIN entry with dots and a number pad. The pad keeps 1-2-3 order in RTL too. */
@Composable
fun PinPad(
    title: String,
    pin: String,
    onPinChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    footer: @Composable () -> Unit = {},
) {
    Column(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🔒", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            message ?: stringResource(R.string.lock_pin_length, PIN_MIN, PIN_MAX),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(maxOf(PIN_MIN, pin.length)) { i ->
                val filled = i < pin.length
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }
        Spacer(Modifier.height(32.dp))
        // Phone keypads are not mirrored in right-to-left languages.
        androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { d -> Key(d, enabled) { if (pin.length < PIN_MAX) onPinChange(pin + d) } }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { if (pin.isNotEmpty()) onPinChange(pin.dropLast(1)) }, enabled = enabled) {
                            Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = stringResource(R.string.lock_delete_digit))
                        }
                    }
                    Key("0", enabled) { if (pin.length < PIN_MAX) onPinChange(pin + "0") }
                    Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = onSubmit, enabled = enabled && pin.length >= PIN_MIN) {
                            Text("OK", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        footer()
    }
}

@Composable
private fun Key(digit: String, enabled: Boolean, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(72.dp), shape = CircleShape) {
        Text(digit, style = MaterialTheme.typography.headlineSmall)
    }
}
