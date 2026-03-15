package com.financasdacasa.app.data.local.mapper

import com.financasdacasa.app.data.local.entity.CachedCategory
import com.financasdacasa.app.data.model.Category

fun Category.toEntity(): CachedCategory = CachedCategory(
    id = id,
    houseId = houseId,
    name = name,
    color = color,
    icon = icon,
    type = type,
    position = position,
    seedKey = seedKey,
    isSystem = isSystem,
    isNameOverridden = isNameOverridden,
)

fun CachedCategory.toDomain(): Category = Category(
    id = id,
    houseId = houseId,
    name = name,
    color = color,
    icon = icon,
    type = type,
    position = position,
    seedKey = seedKey,
    isSystem = isSystem,
    isNameOverridden = isNameOverridden,
)
