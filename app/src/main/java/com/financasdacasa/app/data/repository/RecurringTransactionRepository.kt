package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.RecurringTransactionApi
import com.financasdacasa.app.data.model.CreateRecurringTransactionRequest
import com.financasdacasa.app.data.model.RecurringTransaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTransactionRepository @Inject constructor(
    private val api: RecurringTransactionApi,
) {
    suspend fun create(request: CreateRecurringTransactionRequest): RecurringTransaction =
        api.create(request)

    suspend fun delete(id: String) = api.delete(id)

    suspend fun cancelFrom(id: String, date: String) = api.cancelFrom(id, date)
}
