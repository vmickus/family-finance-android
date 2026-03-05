package com.financasdacasa.app.util

import android.content.Context
import com.financasdacasa.app.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

fun getTodayLocalDate(): String = LocalDate.now().toString()

fun formatTransactionDate(dateStr: String, context: Context): String {
    val date = LocalDate.parse(dateStr)
    val today = LocalDate.now()
    val diff = ChronoUnit.DAYS.between(date, today)
    return when (diff) {
        0L -> context.getString(R.string.today)
        1L -> context.getString(R.string.yesterday)
        else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))
    }
}

fun formatDateHeader(dateStr: String, context: Context): Pair<String, Boolean> {
    val date = LocalDate.parse(dateStr)
    val today = LocalDate.now()
    val isScheduled = date.isAfter(today)
    val label = formatTransactionDate(dateStr, context)
    return label to isScheduled
}

fun getMonthName(month: Int, locale: Locale = Locale.getDefault()): String {
    val date = LocalDate.of(2024, month, 1)
    return date.format(DateTimeFormatter.ofPattern("MMMM", locale))
        .replaceFirstChar { it.uppercase() }
}

fun getDaysInMonth(month: Int, year: Int): Int = LocalDate.of(year, month, 1).lengthOfMonth()
