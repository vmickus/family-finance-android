package com.financasdacasa.app.ui.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.ui.focus.onFocusChanged
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
        if (uiState.isLoading) {
            Column(Modifier.fillMaxSize()) {
                PeriodHeader(
                    month = uiState.month,
                    year = uiState.year,
                    viewMode = uiState.viewMode,
                    onPrevious = { if (uiState.viewMode == ViewMode.MONTHLY) viewModel.previousMonth() else viewModel.previousYear() },
                    onNext = { if (uiState.viewMode == ViewMode.MONTHLY) viewModel.nextMonth() else viewModel.nextYear() },
                    onToggleMode = { viewModel.toggleViewMode() },
                )
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (uiState.viewMode == ViewMode.MONTHLY) {
            MonthlyContent(uiState, viewModel, onDelete = { deletingTransaction = it }, onGoalClick = onGoalClick)
        } else {
            AnnualContent(uiState, viewModel, onDelete = { deletingTransaction = it })
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
    val context = LocalContext.current
    val filtered = if (uiState.typeFilter != null) {
        uiState.transactions.filter { it.type == uiState.typeFilter }
    } else {
        uiState.transactions
    }
    val groups = remember(filtered, uiState.allocations) {
        groupTimelineByDate(filtered, uiState.allocations, context)
    }
    val scheduledGroups = remember(groups) { groups.filter { it.isScheduled } }
    val regularGroups = remember(groups) { groups.filter { !it.isScheduled } }

    val scheduledCollapsed = remember { mutableStateOf(true) }
    val highlightAlpha = remember { Animatable(0f) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item(key = "monthly-period") {
            PeriodHeader(
                month = uiState.month,
                year = uiState.year,
                viewMode = uiState.viewMode,
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() },
                onToggleMode = { viewModel.toggleViewMode() },
            )
        }

        item(key = "monthly-header") {
            Column {
                MonthlyChart(
                    transactions = uiState.transactions,
                    month = uiState.month,
                    year = uiState.year,
                    typeFilter = uiState.typeFilter,
                )

                Spacer(Modifier.height(12.dp))

                uiState.summary?.let { summary ->
                    MonthlySummary(
                        summary = summary,
                        activeFilter = uiState.typeFilter,
                        onFilterChange = { viewModel.onTypeFilterChange(it) },
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
        }

        item(key = "monthly-search") {
            Column {
                CompactSearchField(
                    value = uiState.searchInput,
                    onValueChange = { viewModel.onSearchChange(it) },
                )

                Spacer(Modifier.height(8.dp))
            }
        }

        transactionListItems(
            scheduledGroups = scheduledGroups,
            regularGroups = regularGroups,
            onEdit = { viewModel.showEditForm(it) },
            onDelete = onDelete,
            onGoalClick = onGoalClick,
            scheduledCollapsed = scheduledCollapsed,
            highlightAlpha = highlightAlpha,
            listState = listState,
            coroutineScope = coroutineScope,
        )
    }
}

@Composable
private fun CompactSearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val textStyle = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(containerColor, shape)
                    .then(
                        if (isFocused) Modifier.border(1.5.dp, primaryColor, shape)
                        else Modifier,
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isFocused) primaryColor else placeholderColor,
                )
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            stringResource(R.string.search_transactions),
                            style = MaterialTheme.typography.bodySmall,
                            color = placeholderColor,
                        )
                    }
                    innerTextField()
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
    )
}

@Composable
private fun AnnualContent(
    uiState: HomeUiState,
    viewModel: HomeViewModel,
    onDelete: (Transaction) -> Unit,
) {
    val context = LocalContext.current
    val report = uiState.annualReport ?: return

    val filtered = if (uiState.typeFilter != null) {
        report.transactions.filter { it.type == uiState.typeFilter }
    } else {
        report.transactions
    }
    val groups = remember(filtered) {
        groupTimelineByDate(filtered, emptyList(), context)
    }
    val scheduledGroups = remember(groups) { groups.filter { it.isScheduled } }
    val regularGroups = remember(groups) { groups.filter { !it.isScheduled } }

    val scheduledCollapsed = remember { mutableStateOf(true) }
    val highlightAlpha = remember { Animatable(0f) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item(key = "annual-period") {
            PeriodHeader(
                month = uiState.month,
                year = uiState.year,
                viewMode = uiState.viewMode,
                onPrevious = { viewModel.previousYear() },
                onNext = { viewModel.nextYear() },
                onToggleMode = { viewModel.toggleViewMode() },
            )
        }

        item(key = "annual-search") {
            Column {
                Spacer(Modifier.height(12.dp))

                CompactSearchField(
                    value = uiState.searchInput,
                    onValueChange = { viewModel.onSearchChange(it) },
                )

                Spacer(Modifier.height(8.dp))
            }
        }

        transactionListItems(
            scheduledGroups = scheduledGroups,
            regularGroups = regularGroups,
            onEdit = { viewModel.showEditForm(it) },
            onDelete = onDelete,
            scheduledCollapsed = scheduledCollapsed,
            highlightAlpha = highlightAlpha,
            listState = listState,
            coroutineScope = coroutineScope,
        )
    }
}
