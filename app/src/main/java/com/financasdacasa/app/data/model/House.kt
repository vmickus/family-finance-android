package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class House(
    val id: String,
    val name: String,
    @Json(name = "owner_id") val ownerId: String,
    @Json(name = "created_at") val createdAt: String,
)
