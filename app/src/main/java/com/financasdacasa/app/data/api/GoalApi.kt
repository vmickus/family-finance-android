package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.*
import retrofit2.http.*

interface GoalApi {
    @GET("goals")
    suspend fun list(@Query("house_id") houseId: String): List<Goal>

    @POST("goals")
    suspend fun create(@Body body: CreateGoalRequest): Goal

    @PUT("goals/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateGoalRequest): Goal

    @DELETE("goals/{id}")
    suspend fun delete(@Path("id") id: String)

    @POST("goals/allocations")
    suspend fun createAllocations(@Body body: CreateAllocationRequest): List<GoalAllocation>

    @GET("goals/allocations")
    suspend fun listMonthlyAllocations(
        @Query("house_id") houseId: String,
        @Query("month") month: Int,
        @Query("year") year: Int,
    ): List<GroupedAllocation>

    @GET("goals/{id}/allocations")
    suspend fun listGoalAllocations(@Path("id") goalId: String): List<GoalAllocation>

    @DELETE("goals/allocations/{id}")
    suspend fun deleteAllocation(@Path("id") id: String)
}
