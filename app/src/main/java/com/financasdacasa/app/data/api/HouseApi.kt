package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.House
import com.financasdacasa.app.data.model.HouseMember
import com.financasdacasa.app.data.model.InviteDetails
import com.financasdacasa.app.data.model.InviteResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface HouseApi {
    @GET("houses")
    suspend fun list(): List<House>

    @POST("houses")
    suspend fun create(@Body body: Map<String, Any>): House

    @POST("invites/{token}/accept")
    suspend fun acceptInvite(@Path("token") token: String): House

    @GET("houses/{id}/members")
    suspend fun getMembers(@Path("id") houseId: String): List<HouseMember>

    @POST("houses/{houseId}/invite")
    suspend fun invite(
        @Path("houseId") houseId: String,
        @Body body: Map<String, String>,
    ): InviteResponse

    @DELETE("houses/{houseId}/members/{userId}")
    suspend fun removeMember(
        @Path("houseId") houseId: String,
        @Path("userId") userId: String,
    )

    @PATCH("houses/{houseId}")
    suspend fun rename(
        @Path("houseId") houseId: String,
        @Body body: Map<String, String>,
    ): House

    @GET("invites/{token}")
    suspend fun getInviteDetails(@Path("token") token: String): InviteDetails
}
