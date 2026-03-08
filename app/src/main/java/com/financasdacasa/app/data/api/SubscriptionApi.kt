package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.PaymentEvent
import com.financasdacasa.app.data.model.SubscriptionStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SubscriptionApi {
    @GET("subscriptions/status")
    suspend fun getStatus(): SubscriptionStatusResponse

    @GET("subscriptions/history")
    suspend fun getHistory(): List<PaymentEvent>

    @POST("subscriptions/google-play")
    suspend fun verifyGooglePlay(@Body body: Map<String, String>): Map<String, String>
}
