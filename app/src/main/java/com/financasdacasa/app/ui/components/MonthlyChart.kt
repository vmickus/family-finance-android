package com.financasdacasa.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financasdacasa.app.R
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.util.formatCompactCurrency
import com.financasdacasa.app.util.getDaysInMonth
import java.time.LocalDate

private data class AccumulatedDayData(
    val day: Int,
    val incomeAccum: Double,
    val expenseAccum: Double,
)

private val Emerald = Color(0xFF10B981)
private val Rose = Color(0xFFF43F5E)
private val Amber = Color(0xFFFBBF24)
private val GridColor = Color(0xFFE2E8E4)

private fun buildAccumulatedData(
    transactions: List<Transaction>,
    month: Int,
    year: Int,
): List<AccumulatedDayData> {
    val daysInMonth = getDaysInMonth(month, year)
    val now = LocalDate.now()
    val isCurrentMonth = year == now.year && month == now.monthValue
    val today = if (isCurrentMonth) now.dayOfMonth else daysInMonth

    val byDay = transactions.groupBy {
        it.transactionDate.substringAfterLast("-").toIntOrNull() ?: 1
    }

    // Find last day with data (for current month: at least today)
    var lastDataDay = today
    if (isCurrentMonth) {
        byDay.keys.forEach { day -> if (day > lastDataDay) lastDataDay = day }
    }

    var incomeAccum = 0.0
    var expenseAccum = 0.0
    val result = mutableListOf<AccumulatedDayData>()

    for (day in 1..lastDataDay) {
        val dayTxs = byDay[day] ?: emptyList()
        incomeAccum += dayTxs.filter { it.type == "income" }.sumOf {
            try { it.amount.toDouble() } catch (_: Exception) { 0.0 }
        }
        expenseAccum += dayTxs.filter { it.type == "expense" }.sumOf {
            try { it.amount.toDouble() } catch (_: Exception) { 0.0 }
        }
        result.add(AccumulatedDayData(day, incomeAccum, expenseAccum))
    }

    // Fill remaining days with flat accumulated value
    for (day in (lastDataDay + 1)..daysInMonth) {
        result.add(AccumulatedDayData(day, incomeAccum, expenseAccum))
    }

    return result
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
        buildAccumulatedData(transactions, month, year)
    }

    if (data.all { it.incomeAccum == 0.0 && it.expenseAccum == 0.0 }) {
        Box(modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.chart_no_data),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val now = LocalDate.now()
    val isCurrentMonth = year == now.year && month == now.monthValue
    val today = if (isCurrentMonth) now.dayOfMonth else null
    val daysInMonth = getDaysInMonth(month, year)

    val showIncome = typeFilter != "expense"
    val showExpense = typeFilter != "income"

    val maxValue = remember(data, typeFilter) {
        var m = 0.0
        data.forEach {
            if (showIncome && it.incomeAccum > m) m = it.incomeAccum
            if (showExpense && it.expenseAccum > m) m = it.expenseAccum
        }
        if (m == 0.0) 1.0 else m
    }

    // Y-axis tick values
    val yTicks = remember(maxValue) {
        computeYTicks(maxValue)
    }
    val yMax = remember(yTicks) {
        if (yTicks.isEmpty()) maxValue else yTicks.last() * 1.05
    }

    val xTicks = listOf(1, 7, 14, 21, 28).filter { it <= daysInMonth }

    val textMeasurer = rememberTextMeasurer()
    val axisTextStyle = TextStyle(
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val todayLabel = stringResource(R.string.today)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val leftPadding = 36.dp.toPx()
        val rightPadding = 8.dp.toPx()
        val topPadding = 16.dp.toPx()
        val bottomPadding = 20.dp.toPx()

        val chartLeft = leftPadding
        val chartRight = size.width - rightPadding
        val chartTop = topPadding
        val chartBottom = size.height - bottomPadding
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        fun dayToX(day: Int): Float =
            chartLeft + ((day - 1).toFloat() / (daysInMonth - 1).coerceAtLeast(1)) * chartWidth

        fun valueToY(value: Double): Float =
            chartBottom - ((value / yMax) * chartHeight).toFloat()

        // Draw horizontal grid lines (dashed)
        val gridDash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        yTicks.forEach { tick ->
            val y = valueToY(tick)
            drawLine(
                color = GridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                pathEffect = gridDash,
                strokeWidth = 1f,
            )
        }

        // Draw Y-axis labels
        yTicks.forEach { tick ->
            val y = valueToY(tick)
            val label = formatCompactCurrency(tick)
            val measured = textMeasurer.measure(label, axisTextStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    chartLeft - measured.size.width - 4.dp.toPx(),
                    y - measured.size.height / 2f,
                ),
            )
        }

        // Draw X-axis labels
        xTicks.forEach { day ->
            val x = dayToX(day)
            val label = day.toString()
            val measured = textMeasurer.measure(label, axisTextStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x - measured.size.width / 2f,
                    chartBottom + 4.dp.toPx(),
                ),
            )
        }

        // Draw area + line for income
        if (showIncome) {
            drawAreaLine(
                data = data,
                daysInMonth = daysInMonth,
                valueSelector = { it.incomeAccum },
                lineColor = Emerald,
                fillColorTop = Emerald.copy(alpha = 0.15f),
                fillColorBottom = Emerald.copy(alpha = 0f),
                chartLeft = chartLeft,
                chartWidth = chartWidth,
                chartTop = chartTop,
                chartBottom = chartBottom,
                chartHeight = chartHeight,
                yMax = yMax,
            )
        }

        // Draw area + line for expense
        if (showExpense) {
            drawAreaLine(
                data = data,
                daysInMonth = daysInMonth,
                valueSelector = { it.expenseAccum },
                lineColor = Rose,
                fillColorTop = Rose.copy(alpha = 0.15f),
                fillColorBottom = Rose.copy(alpha = 0f),
                chartLeft = chartLeft,
                chartWidth = chartWidth,
                chartTop = chartTop,
                chartBottom = chartBottom,
                chartHeight = chartHeight,
                yMax = yMax,
            )
        }

        // Draw dashed reference line for today
        if (isCurrentMonth && today != null) {
            val todayX = dayToX(today)
            val todayDash = PathEffect.dashPathEffect(floatArrayOf(12f, 6f))
            drawLine(
                color = Amber,
                start = Offset(todayX, chartTop),
                end = Offset(todayX, chartBottom),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = todayDash,
            )
            // Draw "Hoje" label above the line
            val todayMeasured = textMeasurer.measure(
                todayLabel,
                axisTextStyle.copy(color = Amber, fontSize = 10.sp),
            )
            drawText(
                textLayoutResult = todayMeasured,
                topLeft = Offset(
                    todayX - todayMeasured.size.width / 2f,
                    chartTop - todayMeasured.size.height - 2.dp.toPx(),
                ),
            )
        }
    }
}

private fun DrawScope.drawAreaLine(
    data: List<AccumulatedDayData>,
    daysInMonth: Int,
    valueSelector: (AccumulatedDayData) -> Double,
    lineColor: Color,
    fillColorTop: Color,
    fillColorBottom: Color,
    chartLeft: Float,
    chartWidth: Float,
    chartTop: Float,
    chartBottom: Float,
    chartHeight: Float,
    yMax: Double,
) {
    if (data.isEmpty()) return

    fun dayToX(day: Int): Float =
        chartLeft + ((day - 1).toFloat() / (daysInMonth - 1).coerceAtLeast(1)) * chartWidth

    fun valueToY(value: Double): Float =
        chartBottom - ((value / yMax) * chartHeight).toFloat()

    val points = data.map { Offset(dayToX(it.day), valueToY(valueSelector(it))) }

    // Draw gradient fill
    val fillPath = Path().apply {
        moveTo(points.first().x, chartBottom)
        points.forEach { lineTo(it.x, it.y) }
        lineTo(points.last().x, chartBottom)
        close()
    }
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(fillColorTop, fillColorBottom),
            startY = chartTop,
            endY = chartBottom,
        ),
    )

    // Draw line
    val linePath = Path().apply {
        points.forEachIndexed { i, pt ->
            if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
        }
    }
    drawPath(
        path = linePath,
        color = lineColor,
        style = Stroke(width = 2.dp.toPx()),
    )
}

private fun computeYTicks(maxValue: Double): List<Double> {
    if (maxValue <= 0) return listOf(0.0)
    // Find a nice step: 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, ...
    val rough = maxValue / 4.0
    val magnitude = Math.pow(10.0, Math.floor(Math.log10(rough)))
    val residual = rough / magnitude
    val niceStep = when {
        residual <= 1.0 -> 1.0
        residual <= 2.0 -> 2.0
        residual <= 5.0 -> 5.0
        else -> 10.0
    } * magnitude

    val ticks = mutableListOf<Double>()
    var tick = niceStep
    while (tick <= maxValue * 1.05) {
        ticks.add(tick)
        tick += niceStep
    }
    return ticks
}
