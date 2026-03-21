package com.financasdacasa.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TrendingDown
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.Trophy
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.HouseMember
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.util.formatCurrency
import java.math.BigDecimal

private data class RankedEntry(val name: String, val initial: String, val value: Double)

@Composable
fun HouseContributionsCard(transactions: List<Transaction>, members: List<HouseMember>) {
    if (members.size <= 1 || transactions.isEmpty()) return

    val memberMap = remember(members) {
        members.associate { it.userId to (it.user?.name ?: "?") }
    }

    val (mostRecords, biggestExpenses, biggestIncomes) = remember(transactions, memberMap) {
        buildRankings(transactions, memberMap)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            RankingSection(
                icon = Lucide.Trophy,
                title = stringResource(R.string.dashboard_most_records),
                entries = mostRecords,
                iconColor = MaterialTheme.colorScheme.primary,
                formatValue = { stringResource(R.string.dashboard_records_count, it.toInt()) },
            )
            RankingSection(
                icon = Lucide.TrendingDown,
                title = stringResource(R.string.dashboard_biggest_expense),
                entries = biggestExpenses,
                iconColor = Color(0xFFF43F5E),
                formatValue = { formatCurrency(BigDecimal.valueOf(it)) },
            )
            RankingSection(
                icon = Lucide.TrendingUp,
                title = stringResource(R.string.dashboard_biggest_income),
                entries = biggestIncomes,
                iconColor = Color(0xFF059669),
                formatValue = { formatCurrency(BigDecimal.valueOf(it)) },
            )
        }
    }
}

@Composable
private fun RankingSection(icon: ImageVector, title: String, entries: List<RankedEntry>, iconColor: Color, formatValue: @Composable (Double) -> String) {
    if (entries.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = iconColor)
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        entries.forEachIndexed { i, entry ->
            val isFirst = i == 0
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val avatarSize = if (isFirst) 48.dp else 36.dp
                Box(
                    modifier = Modifier.size(avatarSize).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        entry.initial,
                        style = if (isFirst) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    entry.name,
                    style = if (isFirst) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                    fontWeight = if (isFirst) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isFirst) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    formatValue(entry.value),
                    style = if (isFirst) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isFirst) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun buildRankings(
    transactions: List<Transaction>,
    memberMap: Map<String, String>,
): Triple<List<RankedEntry>, List<RankedEntry>, List<RankedEntry>> {
    val counts = mutableMapOf<String, Int>()
    val maxExpense = mutableMapOf<String, Double>()
    val maxIncome = mutableMapOf<String, Double>()

    for (tx in transactions) {
        counts[tx.userId] = (counts[tx.userId] ?: 0) + 1
        val amount = tx.amount.toDoubleOrNull() ?: 0.0
        if (tx.type == "expense") {
            val cur = maxExpense[tx.userId] ?: 0.0
            if (amount > cur) maxExpense[tx.userId] = amount
        }
        if (tx.type == "income") {
            val cur = maxIncome[tx.userId] ?: 0.0
            if (amount > cur) maxIncome[tx.userId] = amount
        }
    }

    fun toEntry(userId: String, value: Double): RankedEntry {
        val name = memberMap[userId] ?: "?"
        return RankedEntry(name, name.firstOrNull()?.uppercase() ?: "?", value)
    }

    val mostRecords = counts.entries.sortedByDescending { it.value }.take(5).map { toEntry(it.key, it.value.toDouble()) }
    val biggestExpenses = maxExpense.entries.sortedByDescending { it.value }.take(5).map { toEntry(it.key, it.value) }
    val biggestIncomes = maxIncome.entries.sortedByDescending { it.value }.take(5).map { toEntry(it.key, it.value) }

    return Triple(mostRecords, biggestExpenses, biggestIncomes)
}
