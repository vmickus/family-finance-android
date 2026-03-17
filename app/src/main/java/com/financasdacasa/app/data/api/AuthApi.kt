package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.AuthResponse
import com.financasdacasa.app.data.model.AvatarUploadResponse
import com.financasdacasa.app.data.model.DeactivationCheckResponse
import com.financasdacasa.app.data.model.User
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): AvatarUploadResponse

    @DELETE("users/me/avatar")
    suspend fun deleteAvatar()

    @POST("users/me/export")
    suspend fun requestExport()

    @GET("users/me/deactivation-check")
    suspend fun deactivationCheck(): DeactivationCheckResponse

    @POST("users/me/deactivate")
    suspend fun deactivate(@Body body: Map<String, String>)
}
