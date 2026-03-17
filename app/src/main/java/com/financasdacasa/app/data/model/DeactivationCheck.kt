package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeactivationCheckResponse(
    @Json(name = "houses_owned") val housesOwned: List<OwnedHouseInfo>,
)

@JsonClass(generateAdapter = true)
data class OwnedHouseInfo(
    val id: String,
    val name: String,
    val members: List<HouseMemberInfo>,
)

@JsonClass(generateAdapter = true)
data class HouseMemberInfo(
    val id: String,
    val name: String,
    @Json(name = "avatar_url") val avatarUrl: String?,
)
