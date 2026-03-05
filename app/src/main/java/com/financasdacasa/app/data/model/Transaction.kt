package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Transaction(
    val id: String,
    @Json(name = "house_id") val houseId: String,
    @Json(name = "user_id") val userId: String,
    val user: User? = null,
    @Json(name = "category_id") val categoryId: String? = null,
    val category: Category? = null,
    val type: String,
    val amount: String,
    val description: String,
    @Json(name = "transaction_date") val transactionDate: String,
    @Json(name = "recurring_transaction_id") val recurringTransactionId: String? = null,
    @Json(name = "created_at") val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class CreateTransactionRequest(
    @Json(name = "house_id") val houseId: String,
    @Json(name = "category_id") val categoryId: String,
    val type: String,
    val amount: Double,
    val description: String,
    @Json(name = "transaction_date") val transactionDate: String,
)

@JsonClass(generateAdapter = true)
data class UpdateTransactionRequest(
    @Json(name = "category_id") val categoryId: String? = null,
    val type: String? = null,
    val amount: Double? = null,
    val description: String? = null,
    @Json(name = "transaction_date") val transactionDate: String? = null,
)
