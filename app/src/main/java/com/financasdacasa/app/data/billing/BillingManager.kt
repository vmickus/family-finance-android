package com.financasdacasa.app.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.financasdacasa.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

sealed interface PurchaseResult {
    data class Success(val purchaseToken: String, val productId: String) : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Error(val message: String) : PurchaseResult
    data object AlreadyOwned : PurchaseResult
}

data class PendingPurchase(
    val purchaseToken: String,
    val productId: String,
)

data class ProductInfo(
    val productId: String,
    val formattedPrice: String,
    val priceMicros: Long,
    val billingPeriod: String,
)

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "BillingManager"
    }

    private val _products = MutableStateFlow<Map<String, ProductInfo>>(emptyMap())
    val products: StateFlow<Map<String, ProductInfo>> = _products.asStateFlow()

    private var purchaseCallback: ((PurchaseResult) -> Unit)? = null
    private var cachedProductDetails: List<ProductDetails> = emptyList()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val purchase = purchases?.firstOrNull()
                if (purchase != null) {
                    val productId = purchase.products.firstOrNull() ?: ""
                    purchaseCallback?.invoke(PurchaseResult.Success(purchase.purchaseToken, productId))
                } else {
                    purchaseCallback?.invoke(PurchaseResult.Error("No purchase returned"))
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                purchaseCallback?.invoke(PurchaseResult.Cancelled)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                purchaseCallback?.invoke(PurchaseResult.AlreadyOwned)
            }
            else -> {
                purchaseCallback?.invoke(
                    PurchaseResult.Error("Billing error: ${billingResult.debugMessage}")
                )
            }
        }
        purchaseCallback = null
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true

        return withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        cont.resume(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                    }

                    override fun onBillingServiceDisconnected() {
                        Log.w(TAG, "Billing service disconnected")
                    }
                })
            }
        } ?: false
    }

    suspend fun queryProducts(): Map<String, ProductInfo> {
        if (!ensureConnected()) return emptyMap()

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BuildConfig.GP_MONTHLY_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BuildConfig.GP_ANNUAL_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        return suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    cachedProductDetails = productDetailsList
                    val map = mutableMapOf<String, ProductInfo>()
                    productDetailsList.forEach { details ->
                        val pricingPhase = details.subscriptionOfferDetails
                            ?.firstOrNull()
                            ?.pricingPhases
                            ?.pricingPhaseList
                            ?.firstOrNull()
                        if (pricingPhase != null) {
                            map[details.productId] = ProductInfo(
                                productId = details.productId,
                                formattedPrice = pricingPhase.formattedPrice,
                                priceMicros = pricingPhase.priceAmountMicros,
                                billingPeriod = pricingPhase.billingPeriod,
                            )
                        }
                    }
                    _products.value = map
                    cont.resume(map)
                } else {
                    Log.e(TAG, "queryProducts failed: ${billingResult.debugMessage}")
                    cont.resume(emptyMap())
                }
            }
        }
    }

    suspend fun queryPendingPurchases(): List<PendingPurchase> {
        if (!ensureConnected()) return emptyList()

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        return suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val pending = purchases
                        .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
                        .mapNotNull { purchase ->
                            val productId = purchase.products.firstOrNull() ?: return@mapNotNull null
                            PendingPurchase(purchase.purchaseToken, productId)
                        }
                    cont.resume(pending)
                } else {
                    Log.e(TAG, "queryPendingPurchases failed: ${billingResult.debugMessage}")
                    cont.resume(emptyList())
                }
            }
        }
    }

    fun launchPurchaseFlow(
        activity: Activity,
        plan: String,
        onResult: (PurchaseResult) -> Unit,
    ) {
        val productId = when (plan) {
            "monthly" -> BuildConfig.GP_MONTHLY_PRODUCT_ID
            "annual" -> BuildConfig.GP_ANNUAL_PRODUCT_ID
            else -> return onResult(PurchaseResult.Error("Invalid plan: $plan"))
        }

        val productDetails = cachedProductDetails.find { it.productId == productId }
        if (productDetails == null) {
            onResult(PurchaseResult.Error("Product not found. Try again."))
            return
        }

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            onResult(PurchaseResult.Error("No offer available"))
            return
        }

        purchaseCallback = onResult

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            purchaseCallback = null
            onResult(PurchaseResult.Error("Failed to launch billing: ${result.debugMessage}"))
        }
    }
}
