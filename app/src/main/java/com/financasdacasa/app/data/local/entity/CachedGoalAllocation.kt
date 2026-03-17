package com.financasdacasa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_allocations")
data class CachedGoalAllocation(
    @PrimaryKey val id: String,
    val goalId: String,
    val houseId: String,
    val userId: String,
    val amount: String,
    val description: String?,
    val allocationDate: String,
    val createdAt: String,
    // Denormalized user
    val userName: String?,
    val userAvatarUrl: String?,
)
