package com.financasdacasa.data.api

import com.financasdacasa.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("users/me")
    suspend fun getMe(): Response<User>

    // Families
    @GET("families")
    suspend fun listFamilies(): Response<List<Family>>

    @POST("families")
    suspend fun createFamily(@Body request: CreateFamilyRequest): Response<Family>

    @GET("families/{id}")
    suspend fun getFamily(@Path("id") id: String): Response<Family>

    @GET("families/{id}/members")
    suspend fun getFamilyMembers(@Path("id") id: String): Response<List<FamilyMember>>

    @POST("families/{id}/invite")
    suspend fun inviteToFamily(
        @Path("id") id: String,
        @Body request: InviteRequest
    ): Response<InviteResponse>

    @POST("families/{id}/join")
    suspend fun joinFamily(
        @Path("id") id: String,
        @Body request: JoinRequest
    ): Response<Unit>

    // Categories
    @GET("categories")
    suspend fun getCategories(@Query("family_id") familyId: String): Response<List<Category>>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<Category>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<Unit>

    // Transactions
    @GET("transactions")
    suspend fun getTransactions(
        @Query("family_id") familyId: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<List<Transaction>>

    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): Response<Transaction>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): Response<Unit>

    @GET("transactions/summary")
    suspend fun getTransactionSummary(
        @Query("family_id") familyId: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<TransactionSummary>
}
