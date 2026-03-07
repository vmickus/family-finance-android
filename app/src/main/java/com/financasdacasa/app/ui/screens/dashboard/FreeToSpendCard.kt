package com.financasdacasa.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.TransactionSummary
import com.financasdacasa.app.util.formatCurrency
import java.math.BigDecimal
import java.math.RoundingMode

private val roseColor = Color(0xFFF43F5E)
private val amberColor = Color(0xFFD97706)
private val emeraldColor = Color(0xFF10B981)

@Composable
fun FreeToSpendCard(summary: TransactionSummary) {
    val income = try { BigDecimal(summary.totalIncome) } catch (_: Exception) { BigDecimal.ZERO }
    val expense = try { BigDecimal(summary.totalExpense) } catch (_: Exception) { BigDecimal.ZERO }
    val freeToSpend = income.subtract(expense)
    val spentPercent = if (income > BigDecimal.ZERO) {
        expense.multiply(BigDecimal(100)).divide(income, 0, RoundingMode.HALF_UP).toInt()
    } else 0

    val color = when {
        freeToSpend < BigDecimal.ZERO -> roseColor
        spentPercent >= 80 -> amberColor
        else -> emeraldColor
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                stringResource(R.string.dashboard_free_to_spend),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                formatCurrency(freeToSpend),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {}
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (spentPercent.coerceIn(0, 100) / 100f)),
                        color = color,
                        shape = RoundedCornerShape(4.dp),
                    ) {}
                }
                Text(
                    "$spentPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "${stringResource(R.string.dashboard_income_label)}: ${formatCurrency(income)} · ${stringResource(R.string.dashboard_expenses_label)}: ${formatCurrency(expense)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
