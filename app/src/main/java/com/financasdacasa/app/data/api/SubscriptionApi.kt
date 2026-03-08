package com.financasdacasa.app.data.api

import com.financasdacasa.app.data.model.PaymentEvent
import com.financasdacasa.app.data.model.SubscriptionStatusResponse
import retrofit2.http.GET

interface SubscriptionApi {
    @GET("subscriptions/status")
    suspend fun getStatus(): SubscriptionStatusResponse

    @GET("subscriptions/history")
    suspend fun getHistory(): List<PaymentEvent>
}
