package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.RecurringTransactionApi
import com.financasdacasa.app.data.local.dao.RecurringTransactionDao
import com.financasdacasa.app.data.local.mapper.toDomain
import com.financasdacasa.app.data.local.mapper.toEntity
import com.financasdacasa.app.data.model.CreateRecurringTransactionRequest
import com.financasdacasa.app.data.model.RecurringTransaction
import com.financasdacasa.app.data.model.UpdateRecurringTransactionRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTransactionRepository @Inject constructor(
    private val api: RecurringTransactionApi,
    private val recurringTransactionDao: RecurringTransactionDao,
) {
    suspend fun list(houseId: String): List<RecurringTransaction> {
        return try {
            val fresh = api.list(houseId)
            recurringTransactionDao.replaceByHouse(houseId, fresh.map { it.toEntity() })
            fresh
        } catch (e: Exception) {
            val cached = recurringTransactionDao.getByHouse(houseId)
            if (cached.isNotEmpty()) cached.map { it.toDomain() }
            else throw e
        }
    }

    suspend fun create(request: CreateRecurringTransactionRequest): RecurringTransaction =
        api.create(request)

    suspend fun update(id: String, request: UpdateRecurringTransactionRequest): RecurringTransaction =
        api.update(id, request)

    suspend fun delete(id: String) = api.delete(id)

    suspend fun cancelFrom(id: String, date: String) = api.cancelFrom(id, date)
}
