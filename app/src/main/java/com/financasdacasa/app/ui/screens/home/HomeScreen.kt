package com.financasdacasa.app.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.ui.components.*
import com.financasdacasa.app.util.getMonthName

@Composable
fun HomeScreen(
    onGoalClick: ((String) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletingTransaction by remember { mutableStateOf<Transaction?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Period header
            PeriodHeader(
                month = uiState.month,
                year = uiState.year,
                viewMode = uiState.viewMode,
                onPrevious = { if (uiState.viewMode == ViewMode.MONTHLY) viewModel.previousMonth() else viewModel.previousYear() },
                onNext = { if (uiState.viewMode == ViewMode.MONTHLY) viewModel.nextMonth() else viewModel.nextYear() },
                onToggleMode = { viewModel.toggleViewMode() },
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.viewMode == ViewMode.MONTHLY) {
                MonthlyContent(uiState, viewModel, onDelete = { deletingTransaction = it }, onGoalClick = onGoalClick)
            } else {
                AnnualContent(uiState, viewModel, onDelete = { deletingTransaction = it })
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { viewModel.showCreateForm() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_transaction))
        }
    }

    // Transaction form bottom sheet
    if (uiState.showTransactionForm) {
        TransactionFormSheet(
            editTransaction = uiState.editingTransaction,
            onDismiss = { viewModel.dismissForm() },
            onSaved = { viewModel.onTransactionSaved() },
        )
    }

    // Delete dialog
    deletingTransaction?.let { tx ->
        DeleteTransactionDialog(
            transaction = tx,
            onDismiss = { deletingTransaction = null },
            onDeleteSingle = {
                deletingTransaction = null
                viewModel.deleteTransaction(tx.id)
            },
            onCancelRecurring = {
                deletingTransaction = null
                viewModel.cancelRecurring(tx.recurringTransactionId!!, tx.transactionDate)
            },
        )
    }
}

@Composable
private fun PeriodHeader(
    month: Int,
    year: Int,
    viewMode: ViewMode,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleMode: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null)
        }
        Text(
            text = if (viewMode == ViewMode.MONTHLY) "${getMonthName(month)} $year" else "$year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onToggleMode),
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun MonthlyContent(
    uiState: HomeUiState,
    viewModel: HomeViewModel,
    onDelete: (Transaction) -> Unit,
    onGoalClick: ((String) -> Unit)? = null,
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        // Chart
        MonthlyChart(
            transactions = uiState.transactions,
            month = uiState.month,
            year = uiState.year,
            typeFilter = uiState.typeFilter,
        )

        Spacer(Modifier.height(12.dp))

        // Summary
        uiState.summary?.let { summary ->
            MonthlySummary(
                summary = summary,
                activeFilter = uiState.typeFilter,
                onFilterChange = { viewModel.onTypeFilterChange(it) },
            )
        }

        Spacer(Modifier.height(12.dp))

        // Search
        OutlinedTextField(
            value = uiState.searchInput,
            onValueChange = { viewModel.onSearchChange(it) },
            placeholder = { Text(stringResource(R.string.search_transactions)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        // Transaction + allocation list
        TransactionList(
            transactions = uiState.transactions,
            allocations = uiState.allocations,
            typeFilter = uiState.typeFilter,
            onEdit = { viewModel.showEditForm(it) },
            onDelete = onDelete,
            onGoalClick = onGoalClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AnnualContent(
    uiState: HomeUiState,
    viewModel: HomeViewModel,
    onDelete: (Transaction) -> Unit,
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        uiState.annualReport?.let { report ->
            Spacer(Modifier.height(12.dp))

            // Search
            OutlinedTextField(
                value = uiState.searchInput,
                onValueChange = { viewModel.onSearchChange(it) },
                placeholder = { Text(stringResource(R.string.search_transactions)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            TransactionList(
                transactions = report.transactions,
                typeFilter = uiState.typeFilter,
                onEdit = { viewModel.showEditForm(it) },
                onDelete = onDelete,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
