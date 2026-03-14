package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.AnnualReport
import com.financasdacasa.app.data.model.MonthlyHistoryEntry
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.data.model.TransactionSummary
import com.financasdacasa.app.data.model.CreateTransactionRequest
import com.financasdacasa.app.data.model.UpdateTransactionRequest
import com.financasdacasa.app.data.model.YearlySummary
import retrofit2.http.*

interface TransactionApi {
    @GET("transactions")
    suspend fun list(
        @Query("house_id") houseId: String,
        @Query("month") month: Int,
        @Query("year") year: Int,
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: String? = null,
    ): List<Transaction>

    @POST("transactions")
    suspend fun create(@Body body: CreateTransactionRequest): Transaction

    @PUT("transactions/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateTransactionRequest): Transaction

    @DELETE("transactions/{id}")
    suspend fun delete(@Path("id") id: String)

    @GET("transactions/summary")
    suspend fun getSummary(
        @Query("house_id") houseId: String,
        @Query("month") month: Int,
        @Query("year") year: Int,
    ): TransactionSummary

    @GET("transactions/annual")
    suspend fun getAnnualReport(
        @Query("house_id") houseId: String,
        @Query("year") year: Int,
        @Query("type") type: String? = null,
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: String? = null,
    ): AnnualReport

    @GET("transactions/summary")
    suspend fun getYearlySummary(
        @Query("house_id") houseId: String,
        @Query("to") toYear: Int,
    ): List<YearlySummary>

    @GET("transactions/summary")
    suspend fun getMonthlyHistory(
        @Query("house_id") houseId: String,
        @Query("monthly") monthly: String = "true",
        @Query("to_year") toYear: Int,
        @Query("to_month") toMonth: Int,
    ): List<MonthlyHistoryEntry>
}
