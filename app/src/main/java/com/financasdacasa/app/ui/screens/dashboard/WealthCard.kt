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
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.MonthlyHistoryEntry
import com.financasdacasa.app.data.model.YearlySummary
import com.financasdacasa.app.util.formatCurrency
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import java.math.BigDecimal
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

private data class WealthPoint(val label: String, val accumulated: Double)

@Composable
fun WealthCard(
    viewMode: DashboardViewMode,
    yearlySummary: List<YearlySummary>,
    monthlyHistory: List<MonthlyHistoryEntry>,
) {
    val chartData = remember(viewMode, yearlySummary, monthlyHistory) {
        buildWealthData(viewMode, yearlySummary, monthlyHistory)
    }

    if (chartData.size < 2) return

    val totalWealth = chartData.last().accumulated
    val isPositive = totalWealth >= 0
    val color = if (isPositive) Color(0xFF10B981) else Color(0xFFF43F5E)
    val colorInt = if (isPositive) 0xFF10B981.toInt() else 0xFFF43F5E.toInt()
    val areaColorInt = if (isPositive) 0x3310B981 else 0x33F43F5E

    val message = if (!isPositive) {
        stringResource(R.string.dashboard_wealth_negative)
    } else {
        val prev = chartData[chartData.size - 2].accumulated
        if (totalWealth > prev) stringResource(R.string.dashboard_wealth_growing)
        else stringResource(R.string.dashboard_wealth_positive)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(stringResource(R.string.dashboard_wealth_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(formatCurrency(BigDecimal.valueOf(totalWealth)), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = color)
            }
            Text(message, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            val modelProducer = remember { CartesianChartModelProducer() }
            LaunchedEffect(chartData) {
                modelProducer.runTransaction {
                    lineSeries { series(chartData.map { it.accumulated }) }
                }
            }

            val line = LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(Fill(colorInt)),
                stroke = LineCartesianLayer.LineStroke.continuous(thickness = 2.dp),
                areaFill = LineCartesianLayer.AreaFill.single(Fill(areaColorInt)),
            )

            val axisLabel = rememberTextComponent(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textSize = 10.sp,
            )
            val bottomFormatter = CartesianValueFormatter { _, x, _ ->
                chartData.getOrNull(x.toInt())?.label ?: ""
            }
            val dashedGuideline = rememberLineComponent(
                Fill(0xFFE2E8E4.toInt()),
                thickness = 1.dp,
            )

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(line),
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
                modifier = Modifier.fillMaxWidth().height(172.dp),
            )
        }
    }
}

private fun buildWealthData(
    viewMode: DashboardViewMode,
    yearlySummary: List<YearlySummary>,
    monthlyHistory: List<MonthlyHistoryEntry>,
): List<WealthPoint> {
    var running = 0.0
    if (viewMode == DashboardViewMode.ANNUAL) {
        return yearlySummary.map { row ->
            running += (row.income.toDoubleOrNull() ?: 0.0) - (row.expense.toDoubleOrNull() ?: 0.0)
            WealthPoint(label = row.year.toString(), accumulated = running)
        }
    }
    return monthlyHistory.map { row ->
        running += (row.income.toDoubleOrNull() ?: 0.0) - (row.expense.toDoubleOrNull() ?: 0.0)
        val monthShort = Month.of(row.month).getDisplayName(TextStyle.SHORT, Locale.getDefault())
        WealthPoint(label = monthShort, accumulated = running)
    }
}
