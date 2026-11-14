package com.diary.petcare.ui.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.Note
import com.diary.petcare.domain.repository.NoteRepository
import com.diary.petcare.ui.components.DateField
import com.diary.petcare.ui.components.EditScaffold
import com.diary.petcare.ui.components.FormTextField
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.health.AddSectionHeader
import com.diary.petcare.ui.health.RecordCard
import com.diary.petcare.ui.health.SectionEmpty
import com.diary.petcare.ui.navigation.Routes
import com.diary.petcare.ui.profile.ProfileNav
import com.diary.petcare.ui.theme.EventColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesSectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: NoteRepository,
) : ViewModel() {
    val notes: StateFlow<List<Note>> = repo.observeForPet(savedStateHandle.longArg(Routes.ARG_PET_ID))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

/** Diary notes on the Overview tab (behaviour, allergies, anything to remember). */
@Composable
fun NotesSection(nav: ProfileNav, viewModel: NotesSectionViewModel = hiltViewModel()) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    AddSectionHeader(stringResource(R.string.notes_title), onAdd = { nav.editNote(0) })
    if (notes.isEmpty()) SectionEmpty(stringResource(R.string.notes_empty))
    notes.forEach { n ->
        RecordCard(
            emoji = "📝",
            color = EventColors.Note,
            title = n.title,
            subtitle = listOf(Formatters.date(n.date), n.text).filter { it.isNotBlank() }.joinToString(" • "),
            onClick = { nav.editNote(n.id) },
        )
    }
}

data class NoteForm(
    val date: Long = DateTimeUtils.startOfDay(),
    val title: String = "",
    val text: String = "",
    val titleError: Boolean = false,
    val done: Boolean = false,
)

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: NoteRepository,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)
    private val id = savedStateHandle.longArg(Routes.ARG_ID)
    val isEdit = id != 0L
    private val _form = MutableStateFlow(NoteForm())
    val form = _form.asStateFlow()

    init {
        if (isEdit) viewModelScope.launch {
            repo.get(id)?.let { n -> _form.value = NoteForm(n.date, n.title, n.text) }
        }
    }

    fun update(t: (NoteForm) -> NoteForm) = _form.update(t)

    fun save() {
        val f = _form.value
        if (f.title.isBlank()) return _form.update { it.copy(titleError = true) }
        viewModelScope.launch {
            repo.save(Note(id = id, petId = petId, date = f.date, title = f.title.trim(), text = f.text.trim()))
            _form.update { it.copy(done = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            repo.delete(id)
            _form.update { it.copy(done = true) }
        }
    }
}

@Composable
fun NoteEditScreen(onBack: () -> Unit, viewModel: NoteEditViewModel = hiltViewModel()) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(form.done) { if (form.done) onBack() }
    EditScaffold(
        title = stringResource(if (viewModel.isEdit) R.string.note_edit else R.string.note_add),
        onBack = onBack,
        onSave = viewModel::save,
        onDelete = if (viewModel.isEdit) viewModel::delete else null,
    ) {
        DateField(
            label = stringResource(R.string.date),
            value = form.date,
            onValueChange = { v -> if (v != null) viewModel.update { it.copy(date = v) } },
        )
        FormTextField(
            value = form.title,
            onValueChange = { v -> viewModel.update { it.copy(title = v, titleError = false) } },
            label = stringResource(R.string.note_title),
            isError = form.titleError,
            supportingText = if (form.titleError) stringResource(R.string.error_required) else null,
        )
        FormTextField(
            value = form.text,
            onValueChange = { v -> viewModel.update { it.copy(text = v) } },
            label = stringResource(R.string.note_text),
            singleLine = false,
            minLines = 6,
        )
    }
}
