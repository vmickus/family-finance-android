package com.financasdacasa.app.data.local.mapper

import com.financasdacasa.app.data.local.entity.CachedTransaction
import com.financasdacasa.app.data.local.entity.CachedTransactionSummary
import com.financasdacasa.app.data.model.Category
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.data.model.TransactionSummary
import com.financasdacasa.app.data.model.User

fun Transaction.toEntity(month: Int, year: Int): CachedTransaction = CachedTransaction(
    id = id,
    houseId = houseId,
    userId = userId,
    categoryId = categoryId,
    type = type,
    amount = amount,
    description = description,
    transactionDate = transactionDate,
    recurringTransactionId = recurringTransactionId,
    createdAt = createdAt,
    userName = user?.name,
    userAvatarUrl = user?.avatarUrl,
    categoryName = category?.name,
    categoryColor = category?.color,
    categoryIcon = category?.icon,
    categoryType = category?.type,
    month = month,
    year = year,
)

fun CachedTransaction.toDomain(): Transaction = Transaction(
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
    transactionDate = transactionDate,
    recurringTransactionId = recurringTransactionId,
    createdAt = createdAt,
)

fun TransactionSummary.toEntity(houseId: String): CachedTransactionSummary = CachedTransactionSummary(
    houseId = houseId,
    month = month,
    year = year,
    totalIncome = totalIncome,
    totalExpense = totalExpense,
    balance = balance,
)

fun CachedTransactionSummary.toDomain(): TransactionSummary = TransactionSummary(
    totalIncome = totalIncome,
    totalExpense = totalExpense,
    balance = balance,
    month = month,
    year = year,
)
