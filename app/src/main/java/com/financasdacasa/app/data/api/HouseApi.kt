package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.House
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HouseApi {
    @GET("houses")
    suspend fun list(): List<House>

    @POST("houses")
    suspend fun create(@Body body: Map<String, Any>): House

    @POST("invites/{token}/accept")
    suspend fun acceptInvite(@Path("token") token: String): House
}
