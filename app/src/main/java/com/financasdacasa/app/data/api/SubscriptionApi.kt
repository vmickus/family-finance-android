package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.PaymentEvent
import com.financasdacasa.app.data.model.SubscriptionStatusResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class CheckoutResponse(
    @Json(name = "checkout_url") val checkoutUrl: String,
)

interface SubscriptionApi {
    @GET("subscriptions/status")
    suspend fun getStatus(): SubscriptionStatusResponse

    @GET("subscriptions/history")
    suspend fun getHistory(): List<PaymentEvent>

    @POST("subscriptions/google-play")
    suspend fun verifyGooglePlay(@Body body: Map<String, String>): Map<String, String>

    @POST("subscriptions/cancel")
    suspend fun cancel()

    @POST("subscriptions/checkout")
    suspend fun checkout(@Body body: Map<String, String>): CheckoutResponse
}
