package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionSummary(
    @Json(name = "total_income") val totalIncome: String,
    @Json(name = "total_expense") val totalExpense: String,
    val balance: String,
    val month: Int,
    val year: Int,
)

@JsonClass(generateAdapter = true)
data class MonthlyTotal(
    val date: String,
    val income: String,
    val expense: String,
)

@JsonClass(generateAdapter = true)
data class AnnualReport(
    val months: List<MonthlyReportEntry>,
    val transactions: List<Transaction>,
)

@JsonClass(generateAdapter = true)
data class MonthlyReportEntry(
    val month: Int,
    val income: String,
    val expense: String,
)

@JsonClass(generateAdapter = true)
data class YearlySummary(
    val year: Int,
    val income: String,
    val expense: String,
)

@JsonClass(generateAdapter = true)
data class MonthlyHistoryEntry(
    val year: Int,
    val month: Int,
    val income: String,
    val expense: String,
)
