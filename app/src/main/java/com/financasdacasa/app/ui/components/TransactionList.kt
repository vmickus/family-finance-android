package com.financasdacasa.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.util.formatCurrency
import com.financasdacasa.app.util.formatTransactionDate
import com.financasdacasa.app.util.getLucideIcon
import java.math.BigDecimal
import java.time.LocalDate

data class DateGroup(
    val date: String,
    val label: String,
    val isScheduled: Boolean,
    val transactions: List<Transaction>,
    val dailyTotal: BigDecimal,
)

fun groupTransactionsByDate(
    transactions: List<Transaction>,
    context: android.content.Context,
): List<DateGroup> {
    val today = LocalDate.now()
    return transactions
        .groupBy { it.transactionDate }
        .entries
        .sortedByDescending { it.key }
        .map { (date, txs) ->
            val parsed = LocalDate.parse(date)
            val label = formatTransactionDate(date, context)
            val isScheduled = parsed.isAfter(today)
            val total = txs.fold(BigDecimal.ZERO) { acc, tx ->
                val amount = try {
                    BigDecimal(tx.amount)
                } catch (_: Exception) {
                    BigDecimal.ZERO
                }
                if (tx.type == "income") acc + amount else acc - amount
            }
            DateGroup(date, label, isScheduled, txs, total)
        }
}

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    typeFilter: String?,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val filtered = if (typeFilter != null) {
        transactions.filter { it.type == typeFilter }
    } else {
        transactions
    }
    val groups = remember(filtered) { groupTransactionsByDate(filtered, context) }

    if (groups.isEmpty()) {
        Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.no_transactions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        groups.forEach { group ->
            item(key = "header-${group.date}") {
                DateHeader(group)
            }
            items(group.transactions, key = { it.id }) { tx ->
                TransactionRow(tx, onEdit = { onEdit(tx) }, onDelete = { onDelete(tx) })
            }
        }
    }
}

@Composable
private fun DateHeader(group: DateGroup) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                group.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (group.isScheduled) {
                Spacer(Modifier.width(8.dp))
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            stringResource(R.string.scheduled),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }
        }
        val totalColor = if (group.dailyTotal >= BigDecimal.ZERO) {
            Color(0xFF10B981)
        } else {
            Color(0xFFF43F5E)
        }
        Text(
            formatCurrency(group.dailyTotal),
            style = MaterialTheme.typography.labelMedium,
            color = totalColor,
        )
    }
}

@Composable
private fun TransactionRow(
    tx: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = try {
        Color(android.graphics.Color.parseColor(tx.category?.color ?: "#6366f1"))
    } catch (_: Exception) {
        Color(0xFF6366F1)
    }
    val icon = getLucideIcon(tx.category?.icon ?: "Tag")
    val amountColor = if (tx.type == "income") Color(0xFF10B981) else Color(0xFFF43F5E)
    val prefix = if (tx.type == "income") "+ " else "- "

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(tx.description, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text(
                    tx.category?.name ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    prefix + formatCurrency(tx.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                )
                if (tx.recurringTransactionId != null) {
                    Icon(
                        Icons.Default.Repeat,
                        null,
                        Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
