package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.RecurringTransactionApi
import com.financasdacasa.app.data.model.CreateRecurringTransactionRequest
import com.financasdacasa.app.data.model.RecurringTransaction
import com.financasdacasa.app.data.model.UpdateRecurringTransactionRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTransactionRepository @Inject constructor(
    private val api: RecurringTransactionApi,
) {
    suspend fun list(houseId: String): List<RecurringTransaction> = api.list(houseId)

    suspend fun create(request: CreateRecurringTransactionRequest): RecurringTransaction =
        api.create(request)

    suspend fun update(id: String, request: UpdateRecurringTransactionRequest): RecurringTransaction =
        api.update(id, request)

    suspend fun delete(id: String) = api.delete(id)

    suspend fun cancelFrom(id: String, date: String) = api.cancelFrom(id, date)
}
