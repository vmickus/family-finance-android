package com.financasdacasa.app.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.TransactionSummary
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    onCategoryClick: ((String) -> Unit)? = null,
    onViewAllCategories: (() -> Unit)? = null,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Period navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { viewModel.previousPeriod() }) {
                Icon(Lucide.ChevronLeft, contentDescription = null)
            }
            Row(
                modifier = Modifier.clickable { viewModel.toggleViewMode() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val label = if (state.viewMode == DashboardViewMode.ANNUAL) {
                    "${state.year}"
                } else {
                    val monthName = Month.of(state.month)
                        .getDisplayName(TextStyle.FULL, Locale.getDefault())
                        .replaceFirstChar { it.uppercase() }
                    "$monthName ${state.year}"
                }
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Icon(Lucide.ChevronDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = { viewModel.nextPeriod() }) {
                Icon(Lucide.ChevronRight, contentDescription = null)
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (state.error != null) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.dashboard_no_data), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Column
        }

        // Compute active summary for annual mode
        val activeSummary: TransactionSummary? = if (state.viewMode == DashboardViewMode.ANNUAL) {
            state.annualReport?.let { report ->
                val totalIncome = report.months.sumOf { it.income.toDoubleOrNull() ?: 0.0 }
                val totalExpense = report.months.sumOf { it.expense.toDoubleOrNull() ?: 0.0 }
                TransactionSummary(
                    totalIncome = totalIncome.toBigDecimal().toPlainString(),
                    totalExpense = totalExpense.toBigDecimal().toPlainString(),
                    balance = (totalIncome - totalExpense).toBigDecimal().toPlainString(),
                    month = 0,
                    year = state.year,
                )
            }
        } else {
            state.summary
        }

        val activeTransactions = if (state.viewMode == DashboardViewMode.ANNUAL) {
            state.annualReport?.transactions ?: emptyList()
        } else {
            state.transactions
        }

        // 1. Free to Spend
        activeSummary?.let { FreeToSpendCard(it) }

        // 2. Spending by Category
        if (activeTransactions.isNotEmpty()) {
            SpendingByCategoryCard(
                activeTransactions,
                onCategoryClick = onCategoryClick,
                onViewAll = onViewAllCategories,
            )
        }

        // 3. Cash Flow (monthly mode only)
        if (state.viewMode == DashboardViewMode.MONTHLY) {
            CashFlowCard(
                currentYear = state.year,
                currentMonth = state.month,
                currentYearReport = state.annualReport,
                prevYearReport = state.prevYearReport,
            )
        }

        // 4. Wealth Chart
        if (state.yearlySummary.isNotEmpty() || state.monthlyHistory.isNotEmpty()) {
            WealthCard(
                viewMode = state.viewMode,
                yearlySummary = state.yearlySummary,
                monthlyHistory = state.monthlyHistory,
            )
        }

        // 5. House Contributions
        HouseContributionsCard(
            transactions = activeTransactions,
            members = state.members,
        )
    }
}
