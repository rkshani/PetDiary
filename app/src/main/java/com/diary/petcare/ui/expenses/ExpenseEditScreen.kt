package com.diary.petcare.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.Expense
import com.diary.petcare.domain.model.ExpenseCategory
import com.diary.petcare.domain.repository.ExpenseRepository
import com.diary.petcare.ui.components.DateField
import com.diary.petcare.ui.components.EditScaffold
import com.diary.petcare.ui.components.FormTextField
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.emoji
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.format.LocalAppSettings
import com.diary.petcare.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseForm(
    val category: ExpenseCategory = ExpenseCategory.FOOD,
    val amount: String = "",
    val date: Long = DateTimeUtils.startOfDay(),
    val note: String = "",
    val amountError: Boolean = false,
    val done: Boolean = false,
)

@HiltViewModel
class ExpenseEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: ExpenseRepository,
) : ViewModel() {
    private val petId = savedStateHandle.longArg(Routes.ARG_PET_ID)
    private val id = savedStateHandle.longArg(Routes.ARG_ID)
    val isEdit = id != 0L
    private val _form = MutableStateFlow(ExpenseForm())
    val form = _form.asStateFlow()

    init {
        if (isEdit) viewModelScope.launch {
            repo.get(id)?.let { e ->
                _form.value = ExpenseForm(e.category, Formatters.moneyInput(e.amountPaisa), e.date, e.note)
            }
        }
    }

    fun update(t: (ExpenseForm) -> ExpenseForm) = _form.update(t)

    fun save() {
        val f = _form.value
        val paisa = Formatters.parseMoney(f.amount)?.takeIf { it > 0 }
            ?: return _form.update { it.copy(amountError = true) }
        viewModelScope.launch {
            repo.save(Expense(id = id, petId = petId, category = f.category, amountPaisa = paisa, date = f.date, note = f.note.trim()))
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseEditScreen(onBack: () -> Unit, viewModel: ExpenseEditViewModel = hiltViewModel()) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(form.done) { if (form.done) onBack() }
    val currency = LocalAppSettings.current.currency

    EditScaffold(
        title = stringResource(if (viewModel.isEdit) R.string.expense_edit else R.string.expense_add),
        onBack = onBack,
        onSave = viewModel::save,
        onDelete = if (viewModel.isEdit) viewModel::delete else null,
    ) {
        FormTextField(
            value = form.amount,
            onValueChange = { v -> viewModel.update { it.copy(amount = v, amountError = false) } },
            label = stringResource(R.string.expense_amount),
            keyboardType = KeyboardType.Decimal,
            suffix = currency.symbol,
            isError = form.amountError,
            supportingText = if (form.amountError) stringResource(R.string.error_invalid_number) else null,
        )
        SectionHeader(stringResource(R.string.expense_category))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpenseCategory.entries.forEach { c ->
                FilterChip(
                    selected = form.category == c,
                    onClick = { viewModel.update { it.copy(category = c) } },
                    label = { Text("${c.emoji} ${stringResource(c.label)}") },
                )
            }
        }
        DateField(
            label = stringResource(R.string.date),
            value = form.date,
            onValueChange = { v -> if (v != null) viewModel.update { it.copy(date = v) } },
        )
        FormTextField(
            value = form.note,
            onValueChange = { v -> viewModel.update { it.copy(note = v) } },
            label = stringResource(R.string.notes),
        )
    }
}
