package com.financasdacasa.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financasdacasa.app.R
import com.financasdacasa.app.ui.components.TransactionList
import com.financasdacasa.app.util.formatCurrency
import com.financasdacasa.app.util.getLucideIcon
import com.financasdacasa.app.util.getMonthName
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTransactionsScreen(
    onBack: () -> Unit,
    viewModel: CategoryTransactionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val category = state.category

    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category?.color ?: "#9CA3AF"))
    } catch (_: Exception) {
        Color(0xFF9CA3AF)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (category != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    getLucideIcon(category.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = categoryColor,
                                )
                            }
                            Text(
                                category.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // Period navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { viewModel.previousPeriod() }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                }
                Row(
                    modifier = Modifier.clickable { viewModel.toggleViewMode() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val label = if (state.viewMode == DashboardViewMode.ANNUAL) {
                        "${state.year}"
                    } else {
                        "${getMonthName(state.month)} ${state.year}"
                    }
                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = { viewModel.nextPeriod() }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (state.error != null) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.dashboard_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@Column
            }

            val activeTransactions = if (state.viewMode == DashboardViewMode.ANNUAL) {
                state.annualReport?.transactions ?: emptyList()
            } else {
                state.transactions
            }

            // Summary card
            val totalAmount = activeTransactions.fold(BigDecimal.ZERO) { acc, tx ->
                val amount = try { BigDecimal(tx.amount) } catch (_: Exception) { BigDecimal.ZERO }
                acc + amount
            }
            val transactionCount = activeTransactions.size

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            stringResource(R.string.category_total),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            formatCurrency(totalAmount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                        )
                    }
                    Text(
                        stringResource(R.string.category_transactions_count, transactionCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Transaction list
            TransactionList(
                transactions = activeTransactions,
                typeFilter = null,
                onEdit = {},
                onDelete = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
