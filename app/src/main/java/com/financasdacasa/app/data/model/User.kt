package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val email: String,
    val name: String,
    @Json(name = "avatar_url") val avatarUrl: String?,
    @Json(name = "email_verified") val emailVerified: Boolean,
    @Json(name = "is_admin") val isAdmin: Boolean?,
)
