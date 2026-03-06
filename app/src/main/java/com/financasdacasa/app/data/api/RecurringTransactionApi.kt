package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.CreateRecurringTransactionRequest
import com.financasdacasa.app.data.model.RecurringTransaction
import com.financasdacasa.app.data.model.UpdateRecurringTransactionRequest
import retrofit2.http.*

interface RecurringTransactionApi {
    @GET("recurring-transactions")
    suspend fun list(@Query("house_id") houseId: String): List<RecurringTransaction>

    @POST("recurring-transactions")
    suspend fun create(@Body body: CreateRecurringTransactionRequest): RecurringTransaction

    @PUT("recurring-transactions/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateRecurringTransactionRequest): RecurringTransaction

    @DELETE("recurring-transactions/{id}")
    suspend fun delete(@Path("id") id: String)

    @DELETE("recurring-transactions/{id}/from/{date}")
    suspend fun cancelFrom(@Path("id") id: String, @Path("date") date: String)
}
