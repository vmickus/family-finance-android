package com.financasdacasa.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financasdacasa.data.model.Transaction
import com.financasdacasa.data.model.TransactionSummary
import com.financasdacasa.ui.auth.AuthViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val uiState by dashboardViewModel.uiState.collectAsState()

    if (uiState.showFamilySetup) {
        FamilySetupScreen(
            isLoading = uiState.isLoading,
            error = uiState.error,
            onCreateFamily = { dashboardViewModel.createFamily(it) },
            onJoinFamily = { id, token -> dashboardViewModel.joinFamily(id, token) },
            onLogout = { authViewModel.logout() }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Finanças da Casa")
                        uiState.family?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, "Sair")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { dashboardViewModel.showTransactionForm() }) {
                Icon(Icons.Default.Add, "Nova transação")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month Navigation
            MonthNavigation(
                month = uiState.month,
                year = uiState.year,
                onPrevious = { dashboardViewModel.previousMonth() },
                onNext = { dashboardViewModel.nextMonth() }
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Monthly Chart
                    item {
                        MonthlyChart(
                            transactions = uiState.transactions,
                            month = uiState.month,
                            year = uiState.year
                        )
                    }

                    // Summary
                    uiState.summary?.let { summary ->
                        item {
                            SummaryCards(summary)
                        }
                    }

                    // Transactions
                    item {
                        Text(
                            text = "Transações",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (uiState.transactions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Nenhuma transação neste mês",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.transactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onDelete = { dashboardViewModel.deleteTransaction(transaction.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Transaction Form Dialog
    if (uiState.showTransactionForm) {
        TransactionFormDialog(
            categories = uiState.categories,
            isLoading = uiState.isLoading,
            onDismiss = { dashboardViewModel.hideTransactionForm() },
            onCreateTransaction = { categoryId, type, amount, description, date ->
                dashboardViewModel.createTransaction(categoryId, type, amount, description, date)
            },
            onCreateCategory = { name, color ->
                dashboardViewModel.createCategory(name, color)
            }
        )
    }
}

@Composable
private fun MonthNavigation(
    month: Int,
    year: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val months = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, "Mês anterior")
        }
        Text(
            text = "${months[month - 1]} $year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, "Próximo mês")
        }
    }
}

@Composable
private fun SummaryCards(summary: TransactionSummary) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Recebimentos",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    currencyFormat.format(summary.totalIncome.toDoubleOrNull() ?: 0.0),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFEBEE)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = Color(0xFFC62828)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Gastos",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC62828)
                )
                Text(
                    currencyFormat.format(summary.totalExpense.toDoubleOrNull() ?: 0.0),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Saldo",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1565C0)
                )
                Text(
                    currencyFormat.format(summary.balance.toDoubleOrNull() ?: 0.0),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val isIncome = transaction.type == "income"

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        transaction.category?.let { category ->
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        try {
                                            Color(android.graphics.Color.parseColor(category.color))
                                        } catch (e: Exception) {
                                            Color(0xFF6366F1)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                category.name,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (transaction.description.isNotBlank()) {
                        Text(
                            transaction.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        transaction.transactionDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    (if (isIncome) "+" else "-") +
                            currencyFormat.format(transaction.amount.toDoubleOrNull() ?: 0.0),
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
