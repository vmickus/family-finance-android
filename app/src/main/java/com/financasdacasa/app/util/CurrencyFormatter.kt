package com.financasdacasa.app.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val brFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

fun formatCurrency(value: String): String {
    return try {
        brFormat.format(BigDecimal(value))
    } catch (_: Exception) {
        "R$ 0,00"
    }
}

fun formatCurrency(value: BigDecimal): String = brFormat.format(value)

fun formatCompactCurrency(value: Double): String {
    return when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000)
        value >= 1_000 -> String.format("%.1fk", value / 1_000)
        else -> String.format("%.0f", value)
    }
}

fun amountDigitsToDouble(digits: String): Double {
    val cents = digits.toLongOrNull() ?: 0L
    return cents / 100.0
}

fun formatAmountFromDigits(digits: String): String {
    val cents = digits.toLongOrNull() ?: 0L
    val value = BigDecimal(cents).divide(BigDecimal(100))
    return brFormat.format(value)
}
