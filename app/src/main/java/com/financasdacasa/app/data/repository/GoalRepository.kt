package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.GoalApi
import com.financasdacasa.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val api: GoalApi,
) {
    suspend fun list(houseId: String): List<Goal> = api.list(houseId)

    suspend fun create(request: CreateGoalRequest): Goal = api.create(request)

    suspend fun update(id: String, request: UpdateGoalRequest): Goal = api.update(id, request)

    suspend fun delete(id: String) = api.delete(id)

    suspend fun createAllocations(request: CreateAllocationRequest): List<GoalAllocation> =
        api.createAllocations(request)

    suspend fun listMonthlyAllocations(houseId: String, month: Int, year: Int): List<GroupedAllocation> =
        api.listMonthlyAllocations(houseId, month, year)

    suspend fun listGoalAllocations(goalId: String): List<GoalAllocation> =
        api.listGoalAllocations(goalId)

    suspend fun deleteAllocation(id: String) = api.deleteAllocation(id)
}
