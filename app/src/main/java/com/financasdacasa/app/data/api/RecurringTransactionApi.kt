package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.CreateRecurringTransactionRequest
import com.financasdacasa.app.data.model.RecurringTransaction
import retrofit2.http.*

interface RecurringTransactionApi {
    @POST("recurring-transactions")
    suspend fun create(@Body body: CreateRecurringTransactionRequest): RecurringTransaction

    @DELETE("recurring-transactions/{id}")
    suspend fun delete(@Path("id") id: String)

    @DELETE("recurring-transactions/{id}/from/{date}")
    suspend fun cancelFrom(@Path("id") id: String, @Path("date") date: String)
}
