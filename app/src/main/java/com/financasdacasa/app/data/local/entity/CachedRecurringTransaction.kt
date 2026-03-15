package com.financasdacasa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class CachedRecurringTransaction(
    @PrimaryKey val id: String,
    val houseId: String,
    val userId: String,
    val categoryId: String?,
    val type: String,
    val amount: String,
    val description: String,
    val dayOfMonth: Int,
    val startDate: String,
    val occurrences: Int,
    val isActive: Boolean,
    val createdAt: String,
    // Denormalized user
    val userName: String?,
    val userAvatarUrl: String?,
    // Denormalized category
    val categoryName: String?,
    val categoryColor: String?,
    val categoryIcon: String?,
    val categoryType: String?,
)
