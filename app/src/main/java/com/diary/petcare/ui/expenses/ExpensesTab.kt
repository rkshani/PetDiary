package com.diary.petcare.ui.expenses

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.diary.petcare.R
import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.Expense
import com.diary.petcare.domain.repository.ExpenseRepository
import com.diary.petcare.ui.components.EmptyState
import com.diary.petcare.ui.components.LoadingBox
import com.diary.petcare.ui.components.SectionHeader
import com.diary.petcare.ui.components.emoji
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.components.longArg
import com.diary.petcare.ui.format.Formatters
import com.diary.petcare.ui.format.LocalAppSettings
import com.diary.petcare.ui.health.AddSectionHeader
import com.diary.petcare.ui.health.RecordCard
import com.diary.petcare.ui.navigation.Routes
import com.diary.petcare.ui.profile.ProfileNav
import com.diary.petcare.ui.theme.EventColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExpensesTabViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: ExpenseRepository,
) : ViewModel() {
    /** Newest first. Null while loading. */
    val expenses: StateFlow<List<Expense>?> = repo.observeForPet(savedStateHandle.longArg(Routes.ARG_PET_ID))
        .map<List<Expense>, List<Expense>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun ExpensesTab(nav: ProfileNav, viewModel: ExpensesTabViewModel = hiltViewModel()) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val currency = LocalAppSettings.current.currency
    val list = expenses ?: return LoadingBox()
    val byMonth = list.groupBy { DateTimeUtils.startOfMonth(it.date) }.toSortedMap(compareByDescending { it })

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        // EXPENSE_SUMMARY
        item { AddSectionHeader(stringResource(R.string.expenses_title), onAdd = { nav.editExpense(0) }) }
        if (list.isEmpty()) {
            item {
                EmptyState(
                    emoji = "💰",
                    title = stringResource(R.string.expenses_empty_title),
                    message = stringResource(R.string.expenses_empty_message),
                    actionLabel = stringResource(R.string.expense_add),
                    onAction = { nav.editExpense(0) },
                )
            }
        }
        byMonth.forEach { (month, items) ->
            item(key = "m$month") {
                SectionHeader(
                    "${Formatters.monthYear(month)} · ${Formatters.money(items.sumOf { it.amountPaisa }, currency)}",
                )
            }
            items(items, key = { it.id }) { e ->
                RecordCard(
                    emoji = e.category.emoji,
                    color = EventColors.Expense,
                    title = stringResource(e.category.label),
                    subtitle = listOf(Formatters.date(e.date), e.note).filter { it.isNotBlank() }.joinToString(" • "),
                    onClick = { nav.editExpense(e.id) },
                    badge = Formatters.money(e.amountPaisa, currency),
                )
            }
        }
    }
}
