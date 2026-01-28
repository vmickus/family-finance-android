package com.financasdacasa.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financasdacasa.data.model.Transaction
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun MonthlyChart(
    transactions: List<Transaction>,
    month: Int,
    year: Int,
    modifier: Modifier = Modifier
) {
    val chartData = remember(transactions, month, year) {
        calculateChartData(transactions, month, year)
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Evolução do Mês",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Adicione transações para visualizar o gráfico",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                ChartContent(chartData)
            }

            // Legend
            if (transactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFF22C55E), label = "Recebimentos")
                    Spacer(modifier = Modifier.width(24.dp))
                    LegendItem(color = Color(0xFFEF4444), label = "Gastos")
                }
            }
        }
    }
}

@Composable
private fun ChartContent(chartData: ChartData) {
    val incomeEntries = chartData.dailyData.mapIndexed { index, data ->
        FloatEntry(index.toFloat(), data.incomeAccum.toFloat())
    }
    val expenseEntries = chartData.dailyData.mapIndexed { index, data ->
        FloatEntry(index.toFloat(), data.expenseAccum.toFloat())
    }

    val chartEntryModelProducer = remember(incomeEntries, expenseEntries) {
        ChartEntryModelProducer(listOf(incomeEntries, expenseEntries))
    }

    val incomeColor = Color(0xFF22C55E)
    val expenseColor = Color(0xFFEF4444)

    val lineChart = lineChart(
        lines = listOf(
            LineChart.LineSpec(
                lineColor = incomeColor.hashCode(),
                lineBackgroundShader = DynamicShaders.fromBrush(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            incomeColor.copy(alpha = 0.3f),
                            incomeColor.copy(alpha = 0f)
                        )
                    )
                )
            ),
            LineChart.LineSpec(
                lineColor = expenseColor.hashCode(),
                lineBackgroundShader = DynamicShaders.fromBrush(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            expenseColor.copy(alpha = 0.3f),
                            expenseColor.copy(alpha = 0f)
                        )
                    )
                )
            )
        )
    )

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    Chart(
        chart = lineChart,
        chartModelProducer = chartEntryModelProducer,
        startAxis = rememberStartAxis(
            valueFormatter = { value, _ ->
                if (value >= 1000) "R$${(value / 1000).toInt()}k" else "R$${value.toInt()}"
            },
            itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5)
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                val day = chartData.dailyData.getOrNull(value.toInt())?.day ?: (value.toInt() + 1)
                day.toString()
            },
            itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 7)
        ),
        chartScrollState = rememberChartScrollState(),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            shape = MaterialTheme.shapes.small,
            color = color
        ) {}
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class DailyData(
    val day: Int,
    val income: Double,
    val expense: Double,
    val incomeAccum: Double,
    val expenseAccum: Double
)

private data class ChartData(
    val dailyData: List<DailyData>
)

private fun calculateChartData(
    transactions: List<Transaction>,
    month: Int,
    year: Int
): ChartData {
    val yearMonth = YearMonth.of(year, month)
    val daysInMonth = yearMonth.lengthOfMonth()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Initialize daily map
    val dailyMap = mutableMapOf<Int, Pair<Double, Double>>()
    for (day in 1..daysInMonth) {
        dailyMap[day] = Pair(0.0, 0.0)
    }

    // Aggregate transactions by day
    transactions.forEach { transaction ->
        try {
            val date = LocalDate.parse(transaction.transactionDate.substring(0, 10), formatter)
            val day = date.dayOfMonth
            val amount = transaction.amount.toDoubleOrNull() ?: 0.0

            val current = dailyMap[day] ?: Pair(0.0, 0.0)
            if (transaction.type == "income") {
                dailyMap[day] = Pair(current.first + amount, current.second)
            } else {
                dailyMap[day] = Pair(current.first, current.second + amount)
            }
        } catch (e: Exception) {
            // Skip invalid dates
        }
    }

    // Calculate accumulated values
    var incomeAccum = 0.0
    var expenseAccum = 0.0
    val dailyData = mutableListOf<DailyData>()

    for (day in 1..daysInMonth) {
        val (income, expense) = dailyMap[day] ?: Pair(0.0, 0.0)
        incomeAccum += income
        expenseAccum += expense

        dailyData.add(
            DailyData(
                day = day,
                income = income,
                expense = expense,
                incomeAccum = incomeAccum,
                expenseAccum = expenseAccum
            )
        )
    }

    return ChartData(dailyData)
}
