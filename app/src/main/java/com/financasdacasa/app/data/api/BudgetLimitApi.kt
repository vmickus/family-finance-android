package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.BudgetLimit
import com.financasdacasa.app.data.model.UpsertBudgetLimitRequest
import retrofit2.http.*

interface BudgetLimitApi {
    @GET("budget-limits")
    suspend fun list(@Query("house_id") houseId: String): List<BudgetLimit>

    @POST("budget-limits")
    suspend fun upsert(@Body body: UpsertBudgetLimitRequest): BudgetLimit

    @DELETE("budget-limits/{id}")
    suspend fun delete(@Path("id") id: String)
}
