package com.financasdacasa.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "transaction_summaries",
    primaryKeys = ["houseId", "month", "year"],
)
data class CachedTransactionSummary(
    val houseId: String,
    val month: Int,
    val year: Int,
    val totalIncome: String,
    val totalExpense: String,
    val balance: String,
)
