package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.SubscriptionApi
import com.financasdacasa.app.data.model.PaymentEvent
import com.financasdacasa.app.data.model.SubscriptionStatusResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val subscriptionApi: SubscriptionApi,
) {
    suspend fun getStatus(): SubscriptionStatusResponse = subscriptionApi.getStatus()

    suspend fun getHistory(): List<PaymentEvent> = subscriptionApi.getHistory()

    suspend fun verifyGooglePlay(purchaseToken: String, productId: String) {
        subscriptionApi.verifyGooglePlay(
            mapOf("purchase_token" to purchaseToken, "product_id" to productId)
        )
    }

    suspend fun cancel() = subscriptionApi.cancel()

    suspend fun checkout(plan: String): String {
        val response = subscriptionApi.checkout(mapOf("plan" to plan))
        return response.checkoutUrl
    }
}
