package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecurringTransaction(
    val id: String,
    @Json(name = "house_id") val houseId: String,
    @Json(name = "user_id") val userId: String,
    val user: User? = null,
    @Json(name = "category_id") val categoryId: String? = null,
    val category: Category? = null,
    val type: String,
    val amount: String,
    val description: String,
    @Json(name = "day_of_month") val dayOfMonth: Int,
    @Json(name = "start_date") val startDate: String,
    val occurrences: Int,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "created_at") val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class CreateRecurringTransactionRequest(
    @Json(name = "house_id") val houseId: String,
    @Json(name = "category_id") val categoryId: String,
    val type: String,
    val amount: Double,
    val description: String,
    @Json(name = "transaction_date") val transactionDate: String,
    val occurrences: Int,
)

@JsonClass(generateAdapter = true)
data class UpdateRecurringTransactionRequest(
    @Json(name = "category_id") val categoryId: String? = null,
    val amount: Double? = null,
    val description: String? = null,
)
