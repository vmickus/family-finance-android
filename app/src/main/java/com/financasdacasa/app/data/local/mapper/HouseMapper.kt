package com.financasdacasa.app.data.local.mapper

import com.financasdacasa.app.data.local.entity.CachedHouse
import com.financasdacasa.app.data.model.House

fun House.toEntity(): CachedHouse = CachedHouse(
    id = id,
    name = name,
    ownerId = ownerId,
    createdAt = createdAt,
)

fun CachedHouse.toDomain(): House = House(
    id = id,
    name = name,
    ownerId = ownerId,
    createdAt = createdAt,
)
