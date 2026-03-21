package com.financasdacasa.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TrendingDown
import com.composables.icons.lucide.TrendingUp
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.AnnualReport
import com.financasdacasa.app.util.formatCompactCurrency
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.common.Fill
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private data class CashFlowPoint(val label: String, val income: Double, val expense: Double)

@Composable
fun CashFlowCard(
    currentYear: Int,
    currentMonth: Int,
    currentYearReport: AnnualReport?,
    prevYearReport: AnnualReport?,
) {
    val chartData = remember(currentYearReport, prevYearReport, currentMonth, currentYear) {
        buildCashFlowData(currentYearReport, prevYearReport, currentMonth, currentYear)
    }

    val trendPercent = remember(chartData) {
        if (chartData.size < 6) return@remember 0
        val prev5 = chartData.take(5)
        val avgExpense = prev5.map { it.expense }.average()
        val currentExpense = chartData.last().expense
        if (avgExpense > 0) ((currentExpense - avgExpense) / avgExpense * 100).roundToInt() else 0
    }

    val hasData = chartData.any { it.income > 0 || it.expense > 0 }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                stringResource(R.string.dashboard_cash_flow),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))

            if (!hasData) {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.dashboard_no_data), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val modelProducer = remember { CartesianChartModelProducer() }
                LaunchedEffect(chartData) {
                    modelProducer.runTransaction {
                        columnSeries {
                            series(chartData.map { it.income })
                            series(chartData.map { it.expense })
                        }
                    }
                }

                val incomeColumn = rememberLineComponent(Fill(0xFF10B981.toInt()), thickness = 8.dp)
                val expenseColumn = rememberLineComponent(Fill(0xFFF43F5E.toInt()), thickness = 8.dp)

                val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                val axisLabel = rememberTextComponent(color = axisLabelColor, textSize = 10.sp)
                val dashedGuideline = rememberLineComponent(
                    Fill(0xFFE2E8E4.toInt()),
                    thickness = 1.dp,
                )

                val bottomFormatter = CartesianValueFormatter { _, x, _ ->
                    chartData.getOrNull(x.toInt())?.label ?: ""
                }
                val startFormatter = CartesianValueFormatter { _, y, _ ->
                    when {
                        y >= 1_000_000 -> "${(y / 1_000_000).toInt()}M"
                        y >= 1_000 -> "${(y / 1_000).toInt()}k"
                        else -> y.toInt().toString()
                    }
                }

                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            columnProvider = ColumnCartesianLayer.ColumnProvider.series(incomeColumn, expenseColumn),
                        ),
                        startAxis = VerticalAxis.rememberStart(
                            label = axisLabel,
                            line = null,
                            tick = null,
                            guideline = dashedGuideline,
                            valueFormatter = startFormatter,
                            itemPlacer = VerticalAxis.ItemPlacer.count({ 5 }),
                        ),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            label = axisLabel,
                            line = null,
                            tick = null,
                            guideline = null,
                            valueFormatter = bottomFormatter,
                        ),
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                )

                if (trendPercent != 0) {
                    Spacer(Modifier.height(8.dp))
                    val trendColor = if (trendPercent > 0) Color(0xFFF43F5E) else Color(0xFF059669)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            if (trendPercent > 0) Lucide.TrendingUp else Lucide.TrendingDown,
                            contentDescription = null, modifier = Modifier.size(14.dp), tint = trendColor,
                        )
                        Text("${if (trendPercent > 0) "+" else ""}$trendPercent%", style = MaterialTheme.typography.labelSmall, color = trendColor)
                        Text(stringResource(R.string.dashboard_vs_average), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun buildCashFlowData(
    currentYearReport: AnnualReport?,
    prevYearReport: AnnualReport?,
    currentMonth: Int,
    currentYear: Int,
): List<CashFlowPoint> {
    if (currentYearReport == null) return emptyList()

    data class MonthEntry(val month: Int, val year: Int, val income: Double, val expense: Double)
    val allMonths = mutableListOf<MonthEntry>()
    prevYearReport?.months?.forEach { m ->
        allMonths.add(MonthEntry(m.month, currentYear - 1, m.income.toDoubleOrNull() ?: 0.0, m.expense.toDoubleOrNull() ?: 0.0))
    }
    currentYearReport.months.forEach { m ->
        allMonths.add(MonthEntry(m.month, currentYear, m.income.toDoubleOrNull() ?: 0.0, m.expense.toDoubleOrNull() ?: 0.0))
    }

    val target = mutableListOf<Pair<Int, Int>>()
    var m = currentMonth
    var y = currentYear
    for (i in 0 until 6) {
        target.add(0, m to y)
        m--
        if (m == 0) { m = 12; y-- }
    }

    return target.map { (month, year) ->
        val found = allMonths.find { it.month == month && it.year == year }
        val label = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault())
        CashFlowPoint(label, found?.income ?: 0.0, found?.expense ?: 0.0)
    }
}
