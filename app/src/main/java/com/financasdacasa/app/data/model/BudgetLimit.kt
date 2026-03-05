package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BudgetLimit(
    val id: String,
    @Json(name = "house_id") val houseId: String,
    @Json(name = "category_id") val categoryId: String,
    val category: Category? = null,
    @Json(name = "monthly_limit") val monthlyLimit: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
)

@JsonClass(generateAdapter = true)
data class UpsertBudgetLimitRequest(
    @Json(name = "house_id") val houseId: String,
    @Json(name = "category_id") val categoryId: String,
    @Json(name = "monthly_limit") val monthlyLimit: Double,
)
