package com.financasdacasa.app.util

private enum class Op(val precedence: Int) {
    PLUS(1), MINUS(1), TIMES(2), DIVIDE(2);

    fun apply(a: Double, b: Double): Double? = when (this) {
        PLUS -> a + b
        MINUS -> a - b
        TIMES -> a * b
        DIVIDE -> if (b == 0.0) null else a / b
    }
}

private fun Char.toOp(): Op? = when (this) {
    '+' -> Op.PLUS
    '-' -> Op.MINUS
    '*' -> Op.TIMES
    '/' -> Op.DIVIDE
    else -> null
}

fun evaluateExpression(expr: String): Double? {
    val cleaned = expr.replace(",", ".").replace("\\s".toRegex(), "")
    if (cleaned.isEmpty()) return null

    val numbers = mutableListOf<Double>()
    val ops = mutableListOf<Op>()

    fun applyTop(): Boolean {
        if (ops.isEmpty() || numbers.size < 2) return false
        val op = ops.removeLast()
        val b = numbers.removeLast()
        val a = numbers.removeLast()
        val result = op.apply(a, b) ?: return false
        numbers.add(result)
        return true
    }

    var i = 0
    var expectNumber = true

    while (i < cleaned.length) {
        val c = cleaned[i]

        if (expectNumber) {
            // Handle leading sign for this number
            val sign = when {
                c == '-' -> { i++; -1.0 }
                c == '+' -> { i++; 1.0 }
                else -> 1.0
            }
            if (i >= cleaned.length || (!cleaned[i].isDigit() && cleaned[i] != '.')) return null

            val start = i
            var hasDot = false
            while (i < cleaned.length && (cleaned[i].isDigit() || cleaned[i] == '.')) {
                if (cleaned[i] == '.') {
                    if (hasDot) return null // e.g. "1.2.3"
                    hasDot = true
                }
                i++
            }
            val numStr = cleaned.substring(start, i)
            if (numStr == "." || numStr.isEmpty()) return null
            val value = numStr.toDoubleOrNull() ?: return null
            numbers.add(sign * value)
            expectNumber = false
        } else {
            val op = c.toOp() ?: return null
            while (ops.isNotEmpty() && ops.last().precedence >= op.precedence) {
                if (!applyTop()) return null
            }
            ops.add(op)
            i++
            expectNumber = true
        }
    }

    if (expectNumber) return null // trailing operator

    while (ops.isNotEmpty()) {
        if (!applyTop()) return null
    }

    if (numbers.size != 1) return null
    val result = numbers.single()
    if (result.isNaN() || result.isInfinite()) return null
    return result
}

fun hasOperator(expr: String): Boolean {
    val cleaned = expr.replace("\\s".toRegex(), "")
    if (cleaned.isEmpty()) return false

    var i = 0
    // Skip leading sign
    if (i < cleaned.length && (cleaned[i] == '-' || cleaned[i] == '+')) i++
    // Skip first number
    while (i < cleaned.length && (cleaned[i].isDigit() || cleaned[i] == '.' || cleaned[i] == ',')) i++
    // If there's an operator after the first number, we have a binary operator
    return i < cleaned.length && cleaned[i].toOp() != null
}

fun normalizeExpressionInput(raw: String): String =
    raw.filter { it.isDigit() || it in ".,+-*/ " }
