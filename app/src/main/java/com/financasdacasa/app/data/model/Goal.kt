package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Goal(
    val id: String,
    @Json(name = "house_id") val houseId: String,
    val name: String,
    @Json(name = "target_amount") val targetAmount: String,
    @Json(name = "current_amount") val currentAmount: String,
    @Json(name = "plant_type") val plantType: String,
    val color: String,
    @Json(name = "priority_percent") val priorityPercent: Int,
    val deadline: String? = null,
    val status: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
)

@JsonClass(generateAdapter = true)
data class GoalAllocation(
    val id: String,
    @Json(name = "goal_id") val goalId: String,
    @Json(name = "house_id") val houseId: String,
    @Json(name = "user_id") val userId: String,
    val amount: String,
    val description: String? = null,
    @Json(name = "allocation_date") val allocationDate: String,
    @Json(name = "created_at") val createdAt: String,
    val user: User? = null,
)

@JsonClass(generateAdapter = true)
data class AllocationItem(
    val id: String,
    @Json(name = "goal_id") val goalId: String,
    @Json(name = "goal_name") val goalName: String,
    @Json(name = "plant_type") val plantType: String,
    val color: String,
    val amount: String,
    val description: String? = null,
)

@JsonClass(generateAdapter = true)
data class GroupedAllocation(
    val id: String,
    @Json(name = "house_id") val houseId: String,
    @Json(name = "user_id") val userId: String,
    val user: User? = null,
    @Json(name = "allocation_date") val allocationDate: String,
    val items: List<AllocationItem>,
    @Json(name = "total_amount") val totalAmount: String,
    @Json(name = "created_at") val createdAt: String,
)

data class FlatAllocationItem(
    val id: String,
    val goalId: String,
    val goalName: String,
    val plantType: String,
    val color: String,
    val amount: String,
    val description: String?,
    val allocationDate: String,
    val userName: String?,
)

@JsonClass(generateAdapter = true)
data class CreateGoalRequest(
    @Json(name = "house_id") val houseId: String,
    val name: String,
    @Json(name = "target_amount") val targetAmount: Double,
    @Json(name = "plant_type") val plantType: String,
    val color: String,
    @Json(name = "priority_percent") val priorityPercent: Int,
    val deadline: String? = null,
)

@JsonClass(generateAdapter = true)
data class UpdateGoalRequest(
    val name: String? = null,
    @Json(name = "target_amount") val targetAmount: Double? = null,
    @Json(name = "plant_type") val plantType: String? = null,
    val color: String? = null,
    @Json(name = "priority_percent") val priorityPercent: Int? = null,
    val deadline: String? = null,
    val status: String? = null,
)

@JsonClass(generateAdapter = true)
data class CreateAllocationRequest(
    @Json(name = "house_id") val houseId: String,
    @Json(name = "allocation_date") val allocationDate: String,
    val allocations: List<AllocationItemRequest>,
)

@JsonClass(generateAdapter = true)
data class AllocationItemRequest(
    @Json(name = "goal_id") val goalId: String,
    val amount: Double,
    val description: String? = null,
)

@JsonClass(generateAdapter = true)
data class UpdateAllocationRequest(
    @Json(name = "goal_id") val goalId: String? = null,
    val amount: Double? = null,
    val description: String? = null,
)
