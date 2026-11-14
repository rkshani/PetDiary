package com.diary.petcare.ui.lock

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Unlock screen shown over the whole app when the PIN lock is on. */
@Composable
fun LockScreen(checkPin: suspend (String) -> Boolean, onUnlocked: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var wrong by remember { mutableIntStateOf(0) }
    var waitSeconds by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(waitSeconds) {
        if (waitSeconds > 0) {
            delay(1_000)
            waitSeconds--
        }
    }

    val message = when {
        waitSeconds > 0 -> stringResource(R.string.lock_wait, waitSeconds)
        wrong > 0 -> stringResource(R.string.lock_wrong)
        else -> null
    }
    PinPad(
        title = stringResource(R.string.lock_enter_pin),
        pin = pin,
        onPinChange = { pin = it },
        message = message,
        isError = wrong > 0,
        enabled = waitSeconds == 0,
        onSubmit = {
            val entered = pin
            scope.launch {
                if (checkPin(entered)) {
                    onUnlocked()
                } else {
                    pin = ""
                    wrong++
                    if (wrong % MAX_TRIES == 0) waitSeconds = WAIT_SECONDS
                }
            }
        },
        footer = {
            Text(
                stringResource(R.string.lock_forgot_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        },
    )
}

private const val MAX_TRIES = 5
private const val WAIT_SECONDS = 30

@HiltViewModel
class PinSetupViewModel @Inject constructor(private val repo: SettingsRepository) : ViewModel() {
    fun save(pin: String, onDone: () -> Unit) = viewModelScope.launch {
        repo.setPin(pin)
        onDone()
    }
}

/** Two steps: type a new PIN, then type it again. */
@Composable
fun PinSetupScreen(onDone: () -> Unit, viewModel: PinSetupViewModel = hiltViewModel()) {
    var first by rememberSaveable { mutableStateOf<String?>(null) }
    var pin by rememberSaveable { mutableStateOf("") }
    var mismatch by rememberSaveable { mutableStateOf(false) }

    PinPad(
        title = stringResource(if (first == null) R.string.lock_new_pin else R.string.lock_confirm_pin),
        pin = pin,
        onPinChange = { pin = it; mismatch = false },
        message = if (mismatch) stringResource(R.string.lock_mismatch) else null,
        isError = mismatch,
        onSubmit = {
            val f = first
            when {
                f == null -> { first = pin; pin = "" }
                f == pin -> viewModel.save(pin, onDone)
                else -> { first = null; pin = ""; mismatch = true }
            }
        },
    )
}
