package com.diary.petcare.ui.health

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.domain.usecase.DoseItem
import com.diary.petcare.domain.usecase.ObserveDosesUseCase
import com.diary.petcare.domain.usecase.SetDoseGivenUseCase
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayDosesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDoses: ObserveDosesUseCase,
    private val setDoseGiven: SetDoseGivenUseCase,
) : ViewModel() {
    val doses: StateFlow<List<DoseItem>> = observeDoses(petId = savedStateHandle.longArg(Routes.ARG_PET_ID))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggle(item: DoseItem) {
        viewModelScope.launch {
            setDoseGiven(item.medicine.id, item.medicine.petId, item.scheduledAt, !item.given)
        }
    }
}

/** "Today's doses" with a "given" checkbox per dose. Hidden when nothing is due today. */
@Composable
fun TodayDosesSection(viewModel: TodayDosesViewModel = hiltViewModel()) {
    val doses by viewModel.doses.collectAsStateWithLifecycle()
    if (doses.isEmpty()) return
    val given = doses.count { it.given }
    SectionHeader(stringResource(R.string.doses_today, given, doses.size))
    Column {
        doses.forEach { d -> DoseRow(d, onToggle = { viewModel.toggle(d) }) }
    }
}

@Composable
fun DoseRow(item: DoseItem, onToggle: () -> Unit, petName: String? = null) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.given, onCheckedChange = { onToggle() })
        Column(Modifier.weight(1f)) {
            val title = listOfNotNull(petName, item.medicine.name).joinToString(" · ")
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.given) TextDecoration.LineThrough else null,
            )
            Text(
                listOf(Formatters.time(item.scheduledAt), item.medicine.dose).filter { it.isNotBlank() }.joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item.givenAt?.let {
            Text(
                stringResource(R.string.dose_given_at, Formatters.time(it)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}
