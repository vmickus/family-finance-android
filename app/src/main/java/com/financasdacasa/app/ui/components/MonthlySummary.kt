package com.financasdacasa.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.TransactionSummary
import com.financasdacasa.app.util.formatCurrency
import java.math.BigDecimal

private val incomeColor = Color(0xFF059669)
private val expenseColor = Color(0xFFF43F5E)
private val balancePositiveColor = Color(0xFF0284C7)
private val balanceNegativeColor = Color(0xFFD97706)
private val tealColor = Color(0xFF0D9488)

@Composable
fun MonthlySummary(
    summary: TransactionSummary,
    activeFilter: String?,
    onFilterChange: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SummaryCard(
                label = stringResource(R.string.income),
                value = formatCurrency(summary.totalIncome),
                icon = Lucide.TrendingUp,
                color = incomeColor,
                isSelected = activeFilter == "income",
                isFaded = activeFilter != null && activeFilter != "income",
                onClick = { onFilterChange(if (activeFilter == "income") null else "income") },
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                label = stringResource(R.string.expense),
                value = formatCurrency(summary.totalExpense),
                icon = Lucide.TrendingDown,
                color = expenseColor,
                isSelected = activeFilter == "expense",
                isFaded = activeFilter != null && activeFilter != "expense",
                onClick = { onFilterChange(if (activeFilter == "expense") null else "expense") },
                modifier = Modifier.weight(1f),
            )
            val balance = try {
                BigDecimal(summary.balance)
            } catch (_: Exception) {
                BigDecimal.ZERO
            }
            SummaryCard(
                label = stringResource(R.string.balance),
                value = formatCurrency(summary.balance),
                icon = Lucide.Wallet,
                color = if (balance >= BigDecimal.ZERO) balancePositiveColor else balanceNegativeColor,
                isSelected = false,
                isFaded = activeFilter != null,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }

        val allocations = try {
            BigDecimal(summary.totalAllocations)
        } catch (_: Exception) {
            BigDecimal.ZERO
        }
        if (allocations > BigDecimal.ZERO) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Icon(
                    Lucide.Sprout,
                    contentDescription = null,
                    tint = tealColor,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${stringResource(R.string.summary_invested)}: ${formatCurrency(allocations)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = tealColor,
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    isFaded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .alpha(if (isFaded) 0.5f else 1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                color.copy(alpha = 0.1f)
            } else {
                Color.White
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
