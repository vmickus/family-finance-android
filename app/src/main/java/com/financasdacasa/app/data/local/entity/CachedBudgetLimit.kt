package com.financasdacasa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_limits")
data class CachedBudgetLimit(
    @PrimaryKey val id: String,
    val houseId: String,
    val categoryId: String,
    val monthlyLimit: String,
    val createdAt: String,
    val updatedAt: String,
    // Denormalized category
    val categoryName: String?,
    val categoryColor: String?,
    val categoryIcon: String?,
    val categoryType: String?,
)
