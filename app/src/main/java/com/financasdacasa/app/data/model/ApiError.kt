package com.financasdacasa.app.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiError(
    val error: String,
    val code: String?,
)
