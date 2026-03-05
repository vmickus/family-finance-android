package com.financasdacasa.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.util.getDaysInMonth
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries

data class DailyChartData(
    val day: Int,
    val income: Double,
    val expense: Double,
)

fun buildDailyChartData(
    transactions: List<Transaction>,
    month: Int,
    year: Int,
): List<DailyChartData> {
    val daysInMonth = getDaysInMonth(month, year)
    val byDay = transactions.groupBy {
        it.transactionDate.substringAfterLast("-").toIntOrNull() ?: 1
    }
    return (1..daysInMonth).map { day ->
        val dayTxs = byDay[day] ?: emptyList()
        val income = dayTxs.filter { it.type == "income" }.sumOf {
            try {
                it.amount.toDouble()
            } catch (_: Exception) {
                0.0
            }
        }
        val expense = dayTxs.filter { it.type == "expense" }.sumOf {
            try {
                it.amount.toDouble()
            } catch (_: Exception) {
                0.0
            }
        }
        DailyChartData(day, income, expense)
    }
}

@Composable
fun MonthlyChart(
    transactions: List<Transaction>,
    month: Int,
    year: Int,
    typeFilter: String?,
    modifier: Modifier = Modifier,
) {
    val data = remember(transactions, month, year) {
        buildDailyChartData(transactions, month, year)
    }

    if (data.all { it.income == 0.0 && it.expense == 0.0 }) {
        Box(modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.chart_no_data),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data, typeFilter) {
        modelProducer.runTransaction {
            columnSeries {
                if (typeFilter != "expense") {
                    series(data.map { it.income })
                }
                if (typeFilter != "income") {
                    series(data.map { it.expense })
                }
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth().height(180.dp),
    )
}
