package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.BudgetLimitApi
import com.financasdacasa.app.data.model.BudgetLimit
import com.financasdacasa.app.data.model.UpsertBudgetLimitRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetLimitRepository @Inject constructor(
    private val api: BudgetLimitApi,
) {
    suspend fun list(houseId: String): List<BudgetLimit> = api.list(houseId)

    suspend fun upsert(request: UpsertBudgetLimitRequest): BudgetLimit = api.upsert(request)

    suspend fun delete(id: String) = api.delete(id)
}
