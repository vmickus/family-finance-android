package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InviteDetails(
    @Json(name = "house_id") val houseId: String? = null,
    @Json(name = "house_name") val houseName: String? = null,
    @Json(name = "owner_name") val ownerName: String? = null,
    @Json(name = "is_valid") val isValid: Boolean,
    val reason: String? = null,
)
