package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateCategoryRequest(
    @Json(name = "house_id") val houseId: String,
    val name: String,
    val color: String,
    val icon: String,
    val type: String,
)

@JsonClass(generateAdapter = true)
data class UpdateCategoryRequest(
    val name: String? = null,
    val color: String? = null,
    val icon: String? = null,
)

@JsonClass(generateAdapter = true)
data class ReorderCategoriesRequest(
    @Json(name = "house_id") val houseId: String,
    val type: String,
    @Json(name = "category_ids") val categoryIds: List<String>,
)
