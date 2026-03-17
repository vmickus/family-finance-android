package com.financasdacasa.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.Droplets
import com.composables.icons.lucide.Lucide
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.FlatAllocationItem
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.util.formatCurrency
import com.financasdacasa.app.util.formatTransactionDate
import com.financasdacasa.app.util.getLucideIcon
import com.financasdacasa.app.util.getPlantDrawable
import kotlinx.coroutines.launch
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

    val scheduledGroups = remember(groups) { groups.filter { it.isScheduled } }
    val regularGroups = remember(groups) { groups.filter { !it.isScheduled } }

    var scheduledCollapsed by remember { mutableStateOf(true) }
    val highlightAlpha = remember { Animatable(0f) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val primary = MaterialTheme.colorScheme.primary

    val chevronRotation by animateFloatAsState(
        targetValue = if (scheduledCollapsed) 0f else 180f,
        animationSpec = tween(durationMillis = 300),
        label = "chevronRotation",
    )

    val scheduledTxCount = remember(scheduledGroups) {
        scheduledGroups.sumOf { g -> g.entries.filterIsInstance<TransactionEntry>().size }
    }
    val scheduledTotal = remember(scheduledGroups) {
        scheduledGroups.fold(BigDecimal.ZERO) { acc, g -> acc + g.dailyTotal }
    }

    LazyColumn(state = listState, modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (scheduledGroups.isNotEmpty()) {
            item(key = "scheduled-toggle") {
                val isExpanded = !scheduledCollapsed
                val buttonContainer = if (isExpanded) {
                    primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
                val buttonBorder = if (isExpanded) {
                    primary.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                }
                val buttonContentAlpha = if (isExpanded) 1f else 0.7f

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val expanding = scheduledCollapsed
                            scheduledCollapsed = !scheduledCollapsed
                            if (expanding) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
                                coroutineScope.launch {
                                    highlightAlpha.snapTo(0.12f)
                                    highlightAlpha.animateTo(0f, animationSpec = tween(800))
                                }
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = buttonContainer,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        buttonBorder,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                Lucide.CalendarClock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = buttonContentAlpha),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.scheduled_summary, scheduledTxCount),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = buttonContentAlpha),
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val totalColor = if (scheduledTotal >= BigDecimal.ZERO) {
                                Color(0xFF10B981)
                            } else {
                                Color(0xFFF43F5E)
                            }
                            Text(
                                formatCurrency(scheduledTotal.abs()),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = totalColor.copy(alpha = buttonContentAlpha),
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(chevronRotation),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = buttonContentAlpha),
                            )
                        }
                    }
                }
            }

            item(key = "scheduled-section") {
                AnimatedVisibility(
                    visible = !scheduledCollapsed,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300)),
                ) {
                    Column(
                        modifier = Modifier.padding(bottom = 12.dp, end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        scheduledGroups.forEach { group ->
                            DateHeader(group)
                            group.entries.forEach { entry ->
                                when (entry) {
                                    is TransactionEntry -> TransactionRow(
                                        tx = entry.transaction,
                                        onEdit = { onEdit(entry.transaction) },
                                        onDelete = { onDelete(entry.transaction) },
                                        highlightAlpha = highlightAlpha.value,
                                        highlightColor = primary,
                                    )
                                    is AllocationEntry -> AllocationTimelineCard(
                                        alloc = entry.allocation,
                                        onClick = { onGoalClick?.invoke(entry.allocation.goalId) },
                                        highlightAlpha = highlightAlpha.value,
                                        highlightColor = primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        regularGroups.forEach { group ->
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
                        tx = entry.transaction,
                        onEdit = { onEdit(entry.transaction) },
                        onDelete = { onDelete(entry.transaction) },
                    )
                    is AllocationEntry -> AllocationTimelineCard(
                        alloc = entry.allocation,
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
    highlightAlpha: Float = 0f,
    highlightColor: Color = Color.Transparent,
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
        Box {
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
            if (highlightAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(highlightColor.copy(alpha = highlightAlpha)),
                )
            }
        }
    }
}

@Composable
private fun AllocationTimelineCard(
    alloc: FlatAllocationItem,
    onClick: () -> Unit,
    highlightAlpha: Float = 0f,
    highlightColor: Color = Color.Transparent,
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
        Box {
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
            if (highlightAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(highlightColor.copy(alpha = highlightAlpha)),
                )
            }
        }
    }
}
