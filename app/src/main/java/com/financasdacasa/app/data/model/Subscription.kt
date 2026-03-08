package com.financasdacasa.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionStatusResponse(
    val status: String,
    val plan: String,
    val provider: String?,
    @Json(name = "trial_ends_at") val trialEndsAt: String?,
    @Json(name = "trial_days_remaining") val trialDaysRemaining: Int,
    @Json(name = "current_period_end") val currentPeriodEnd: String?,
    @Json(name = "is_accessible") val isAccessible: Boolean,
)

@JsonClass(generateAdapter = true)
data class PaymentEvent(
    val id: String,
    @Json(name = "subscription_id") val subscriptionId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "event_type") val eventType: String,
    @Json(name = "provider_payment_id") val providerPaymentId: String?,
    val amount: String?,
    val currency: String,
    @Json(name = "created_at") val createdAt: String,
)
