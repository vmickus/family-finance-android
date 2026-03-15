package com.financasdacasa.app.data.local.mapper

import com.financasdacasa.app.data.local.entity.CachedGoal
import com.financasdacasa.app.data.local.entity.CachedGoalAllocation
import com.financasdacasa.app.data.model.Goal
import com.financasdacasa.app.data.model.GoalAllocation
import com.financasdacasa.app.data.model.User

fun Goal.toEntity(): CachedGoal = CachedGoal(
    id = id,
    houseId = houseId,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    plantType = plantType,
    color = color,
    priorityPercent = priorityPercent,
    deadline = deadline,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun CachedGoal.toDomain(): Goal = Goal(
    id = id,
    houseId = houseId,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    plantType = plantType,
    color = color,
    priorityPercent = priorityPercent,
    deadline = deadline,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun GoalAllocation.toEntity(): CachedGoalAllocation = CachedGoalAllocation(
    id = id,
    goalId = goalId,
    houseId = houseId,
    userId = userId,
    amount = amount,
    allocationDate = allocationDate,
    createdAt = createdAt,
    userName = user?.name,
    userAvatarUrl = user?.avatarUrl,
)

fun CachedGoalAllocation.toDomain(): GoalAllocation = GoalAllocation(
    id = id,
    goalId = goalId,
    houseId = houseId,
    userId = userId,
    amount = amount,
    allocationDate = allocationDate,
    createdAt = createdAt,
    user = userName?.let { User(id = userId, email = "", name = it, avatarUrl = userAvatarUrl, emailVerified = true, isAdmin = null) },
)
