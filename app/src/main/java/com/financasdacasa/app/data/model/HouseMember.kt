package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HouseMember(
    val id: String,
    val name: String,
    val email: String,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    val role: String,
)
