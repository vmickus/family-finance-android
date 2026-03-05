package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.AuthResponse
import com.financasdacasa.app.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: Map<String, String>): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): AuthResponse

    @POST("auth/google")
    suspend fun googleLogin(@Body body: Map<String, String>): AuthResponse

    @GET("auth/verify-email")
    suspend fun verifyEmail(@retrofit2.http.Query("token") token: String): Map<String, String>

    @POST("auth/resend-verification")
    suspend fun resendVerification(): Map<String, String>

    @GET("users/me")
    suspend fun getMe(): User
}
