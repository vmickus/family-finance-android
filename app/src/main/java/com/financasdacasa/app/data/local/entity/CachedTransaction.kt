package com.financasdacasa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class CachedTransaction(
    @PrimaryKey val id: String,
    val houseId: String,
    val userId: String,
    val categoryId: String?,
    val type: String,
    val amount: String,
    val description: String,
    val transactionDate: String,
    val recurringTransactionId: String?,
    val createdAt: String,
    // Denormalized user
    val userName: String?,
    val userAvatarUrl: String?,
    // Denormalized category
    val categoryName: String?,
    val categoryColor: String?,
    val categoryIcon: String?,
    val categoryType: String?,
    // Cache key
    val month: Int,
    val year: Int,
)
