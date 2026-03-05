package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Category(
    val id: String,
    @Json(name = "house_id") val houseId: String,
    val name: String,
    val color: String,
    val icon: String,
    val type: String,
    val position: Int,
    @Json(name = "seed_key") val seedKey: String? = null,
    @Json(name = "is_system") val isSystem: Boolean = false,
    @Json(name = "is_name_overridden") val isNameOverridden: Boolean = false,
)
