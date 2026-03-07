package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InviteResponse(
    val token: String,
    @Json(name = "expires_at") val expiresAt: String,
    @Json(name = "invite_url") val inviteUrl: String? = null,
)
