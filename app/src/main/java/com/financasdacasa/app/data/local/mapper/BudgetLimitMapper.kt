package com.financasdacasa.app.data.local.mapper

import com.financasdacasa.app.data.local.entity.CachedBudgetLimit
import com.financasdacasa.app.data.model.BudgetLimit
import com.financasdacasa.app.data.model.Category

fun BudgetLimit.toEntity(): CachedBudgetLimit = CachedBudgetLimit(
    id = id,
    houseId = houseId,
    categoryId = categoryId,
    monthlyLimit = monthlyLimit,
    createdAt = createdAt,
    updatedAt = updatedAt,
    categoryName = category?.name,
    categoryColor = category?.color,
    categoryIcon = category?.icon,
    categoryType = category?.type,
)

fun CachedBudgetLimit.toDomain(): BudgetLimit = BudgetLimit(
    id = id,
    houseId = houseId,
    categoryId = categoryId,
    category = categoryName?.let {
        Category(
            id = categoryId,
            houseId = houseId,
            name = it,
            color = categoryColor ?: "",
            icon = categoryIcon ?: "",
            type = categoryType ?: "",
            position = 0,
        )
    },
    monthlyLimit = monthlyLimit,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
