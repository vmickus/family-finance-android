package com.financasdacasa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class CachedGoal(
    @PrimaryKey val id: String,
    val houseId: String,
    val name: String,
    val targetAmount: String,
    val currentAmount: String,
    val plantType: String,
    val color: String,
    val priorityPercent: Int,
    val deadline: String?,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
)
