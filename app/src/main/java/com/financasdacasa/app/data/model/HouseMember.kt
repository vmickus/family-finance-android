package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HouseMember(
    val id: String,
    @Json(name = "house_id") val houseId: String,
    @Json(name = "user_id") val userId: String,
    val role: String,
    @Json(name = "joined_at") val joinedAt: String,
    val user: User? = null,
)
