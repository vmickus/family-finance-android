package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.GoalApi
import com.financasdacasa.app.data.local.dao.GoalAllocationDao
import com.financasdacasa.app.data.local.dao.GoalDao
import com.financasdacasa.app.data.local.mapper.toDomain
import com.financasdacasa.app.data.local.mapper.toEntity
import com.financasdacasa.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val api: GoalApi,
    private val goalDao: GoalDao,
    private val goalAllocationDao: GoalAllocationDao,
) {
    suspend fun list(houseId: String): List<Goal> {
        return try {
            val fresh = api.list(houseId)
            goalDao.replaceByHouse(houseId, fresh.map { it.toEntity() })
            fresh
        } catch (e: Exception) {
            val cached = goalDao.getByHouse(houseId)
            if (cached.isNotEmpty()) cached.map { it.toDomain() }
            else throw e
        }
    }

    suspend fun create(request: CreateGoalRequest): Goal = api.create(request)

    suspend fun update(id: String, request: UpdateGoalRequest): Goal = api.update(id, request)

    suspend fun delete(id: String) = api.delete(id)

    suspend fun createAllocations(request: CreateAllocationRequest): List<GoalAllocation> =
        api.createAllocations(request)

    suspend fun listMonthlyAllocations(houseId: String, month: Int, year: Int): List<GroupedAllocation> =
        api.listMonthlyAllocations(houseId, month, year)

    suspend fun listGoalAllocations(goalId: String): List<GoalAllocation> {
        return try {
            val fresh = api.listGoalAllocations(goalId)
            goalAllocationDao.replaceByGoal(goalId, fresh.map { it.toEntity() })
            fresh
        } catch (e: Exception) {
            val cached = goalAllocationDao.getByGoal(goalId)
            if (cached.isNotEmpty()) cached.map { it.toDomain() }
            else throw e
        }
    }

    suspend fun updateAllocation(id: String, request: UpdateAllocationRequest): GoalAllocation =
        api.updateAllocation(id, request)

    suspend fun deleteAllocation(id: String) = api.deleteAllocation(id)
}
