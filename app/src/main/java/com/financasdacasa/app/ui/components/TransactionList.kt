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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Droplets
import com.composables.icons.lucide.Lucide
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.FlatAllocationItem
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.util.formatCurrency
import com.financasdacasa.app.util.formatTransactionDate
import com.financasdacasa.app.util.getLucideIcon
import com.financasdacasa.app.util.getPlantDrawable
import java.math.BigDecimal
import java.time.LocalDate

sealed interface TimelineEntry {
    val date: String
    val sortKey: String
}

data class TransactionEntry(val transaction: Transaction) : TimelineEntry {
    override val date: String get() = transaction.transactionDate
    override val sortKey: String get() = transaction.createdAt
}

data class AllocationEntry(val allocation: FlatAllocationItem) : TimelineEntry {
    override val date: String get() = allocation.allocationDate
    override val sortKey: String get() = allocation.id
}

data class DateGroup(
    val date: String,
    val label: String,
    val isScheduled: Boolean,
    val entries: List<TimelineEntry>,
    val dailyTotal: BigDecimal,
)

fun groupTimelineByDate(
    transactions: List<Transaction>,
    allocations: List<FlatAllocationItem>,
    context: android.content.Context,
): List<DateGroup> {
    val today = LocalDate.now()
    val txEntries = transactions.map { TransactionEntry(it) }
    val allocEntries = allocations.map { AllocationEntry(it) }
    val allEntries = (txEntries + allocEntries)

    return allEntries
        .groupBy { it.date }
        .entries
        .sortedByDescending { it.key }
        .map { (date, entries) ->
            val parsed = LocalDate.parse(date)
            val label = formatTransactionDate(date, context)
            val isScheduled = parsed.isAfter(today)
            val total = entries.fold(BigDecimal.ZERO) { acc, entry ->
                when (entry) {
                    is TransactionEntry -> {
                        val amount = try { BigDecimal(entry.transaction.amount) } catch (_: Exception) { BigDecimal.ZERO }
                        if (entry.transaction.type == "income") acc + amount else acc - amount
                    }
                    is AllocationEntry -> {
                        val amount = try { BigDecimal(entry.allocation.amount) } catch (_: Exception) { BigDecimal.ZERO }
                        acc - amount
                    }
                }
            }
            DateGroup(date, label, isScheduled, entries, total)
        }
}

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    allocations: List<FlatAllocationItem> = emptyList(),
    typeFilter: String?,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    onGoalClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val filtered = if (typeFilter != null) {
        transactions.filter { it.type == typeFilter }
    } else {
        transactions
    }
    val groups = remember(filtered, allocations) { groupTimelineByDate(filtered, allocations, context) }

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
            items(group.entries, key = { entry ->
                when (entry) {
                    is TransactionEntry -> "tx-${entry.transaction.id}"
                    is AllocationEntry -> "alloc-${entry.allocation.id}"
                }
            }) { entry ->
                when (entry) {
                    is TransactionEntry -> TransactionRow(
                        entry.transaction,
                        onEdit = { onEdit(entry.transaction) },
                        onDelete = { onDelete(entry.transaction) },
                    )
                    is AllocationEntry -> AllocationTimelineCard(
                        entry.allocation,
                        onClick = { onGoalClick?.invoke(entry.allocation.goalId) },
                    )
                }
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

@Composable
private fun AllocationTimelineCard(
    alloc: FlatAllocationItem,
    onClick: () -> Unit,
) {
    val goalColor = try {
        Color(android.graphics.Color.parseColor(alloc.color))
    } catch (_: Exception) {
        Color(0xFF5B8A72)
    }
    val tealColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = tealColor.copy(alpha = 0.05f),
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(tealColor.copy(alpha = 0.2f)),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Plant icon
            androidx.compose.foundation.Image(
                painter = painterResource(getPlantDrawable(alloc.plantType, 0.5)),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alloc.goalName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!alloc.description.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Lucide.Droplets,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            alloc.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Text(
                "- " + formatCurrency(alloc.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = tealColor,
            )
        }
    }
}
