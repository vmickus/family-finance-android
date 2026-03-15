package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.BudgetLimitApi
import com.financasdacasa.app.data.local.dao.BudgetLimitDao
import com.financasdacasa.app.data.local.mapper.toDomain
import com.financasdacasa.app.data.local.mapper.toEntity
import com.financasdacasa.app.data.model.BudgetLimit
import com.financasdacasa.app.data.model.UpsertBudgetLimitRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetLimitRepository @Inject constructor(
    private val api: BudgetLimitApi,
    private val budgetLimitDao: BudgetLimitDao,
) {
    suspend fun list(houseId: String): List<BudgetLimit> {
        return try {
            val fresh = api.list(houseId)
            budgetLimitDao.replaceByHouse(houseId, fresh.map { it.toEntity() })
            fresh
        } catch (e: Exception) {
            val cached = budgetLimitDao.getByHouse(houseId)
            if (cached.isNotEmpty()) cached.map { it.toDomain() }
            else throw e
        }
    }

    suspend fun upsert(request: UpsertBudgetLimitRequest): BudgetLimit = api.upsert(request)

    suspend fun delete(id: String) = api.delete(id)
}
