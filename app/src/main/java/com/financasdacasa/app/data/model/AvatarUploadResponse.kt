package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AvatarUploadResponse(
    @Json(name = "avatar_url") val avatarUrl: String,
)
