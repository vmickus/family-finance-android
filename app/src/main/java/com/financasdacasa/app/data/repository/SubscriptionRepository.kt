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
}
