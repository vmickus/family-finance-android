package com.financasdacasa.app.data.local.mapper

import com.financasdacasa.app.data.local.entity.CachedRecurringTransaction
import com.financasdacasa.app.data.model.Category
import com.financasdacasa.app.data.model.RecurringTransaction
import com.financasdacasa.app.data.model.User

fun RecurringTransaction.toEntity(): CachedRecurringTransaction = CachedRecurringTransaction(
    id = id,
    houseId = houseId,
    userId = userId,
    categoryId = categoryId,
    type = type,
    amount = amount,
    description = description,
    dayOfMonth = dayOfMonth,
    startDate = startDate,
    occurrences = occurrences,
    isActive = isActive,
    createdAt = createdAt,
    userName = user?.name,
    userAvatarUrl = user?.avatarUrl,
    categoryName = category?.name,
    categoryColor = category?.color,
    categoryIcon = category?.icon,
    categoryType = category?.type,
)

fun CachedRecurringTransaction.toDomain(): RecurringTransaction = RecurringTransaction(
    id = id,
    houseId = houseId,
    userId = userId,
    user = userName?.let { User(id = userId, email = "", name = it, avatarUrl = userAvatarUrl, emailVerified = true, isAdmin = null) },
    categoryId = categoryId,
    category = if (categoryId != null && categoryName != null) Category(
        id = categoryId,
        houseId = houseId,
        name = categoryName,
        color = categoryColor ?: "",
        icon = categoryIcon ?: "",
        type = categoryType ?: "",
        position = 0,
    ) else null,
    type = type,
    amount = amount,
    description = description,
    dayOfMonth = dayOfMonth,
    startDate = startDate,
    occurrences = occurrences,
    isActive = isActive,
    createdAt = createdAt,
)
