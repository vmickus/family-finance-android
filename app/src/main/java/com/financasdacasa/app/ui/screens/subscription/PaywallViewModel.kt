package com.financasdacasa.app.ui.screens.subscription

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.BuildConfig
import com.financasdacasa.app.data.billing.BillingManager
import com.financasdacasa.app.data.billing.PendingPurchase
import com.financasdacasa.app.data.billing.ProductInfo
import com.financasdacasa.app.data.billing.PurchaseResult
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val isLoading: Boolean = true,
    val billingAvailable: Boolean = false,
    val monthlyProduct: ProductInfo? = null,
    val annualProduct: ProductInfo? = null,
    val isPurchasing: Boolean = false,
    val error: String? = null,
    val purchaseSuccess: Boolean = false,
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val subscriptionRepository: SubscriptionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    companion object {
        private const val TAG = "PaywallViewModel"
    }

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        processPendingPurchases()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val products = billingManager.queryProducts()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    billingAvailable = products.isNotEmpty(),
                    monthlyProduct = products[BuildConfig.GP_MONTHLY_PRODUCT_ID],
                    annualProduct = products[BuildConfig.GP_ANNUAL_PRODUCT_ID],
                )
            }
        }
    }

    private fun processPendingPurchases() {
        viewModelScope.launch {
            val pending = billingManager.queryPendingPurchases()
            pending.forEach { verifyWithBackend(it) }
        }
    }

    private suspend fun verifyWithBackend(purchase: PendingPurchase) {
        try {
            subscriptionRepository.verifyGooglePlay(purchase.purchaseToken, purchase.productId)
            sessionManager.clearSubscriptionExpired()
            _uiState.update { it.copy(purchaseSuccess = true) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify purchase: ${purchase.productId}", e)
        }
    }

    fun purchase(activity: Activity, plan: String) {
        _uiState.update { it.copy(isPurchasing = true, error = null) }

        billingManager.launchPurchaseFlow(activity, plan) { result ->
            when (result) {
                is PurchaseResult.Success -> {
                    viewModelScope.launch {
                        try {
                            subscriptionRepository.verifyGooglePlay(
                                result.purchaseToken,
                                result.productId,
                            )
                            sessionManager.clearSubscriptionExpired()
                            _uiState.update {
                                it.copy(isPurchasing = false, purchaseSuccess = true)
                            }
                        } catch (e: Exception) {
                            _uiState.update {
                                it.copy(isPurchasing = false, error = "VERIFICATION_FAILED")
                            }
                        }
                    }
                }
                is PurchaseResult.Cancelled -> {
                    _uiState.update { it.copy(isPurchasing = false) }
                }
                is PurchaseResult.AlreadyOwned -> {
                    viewModelScope.launch {
                        val pending = billingManager.queryPendingPurchases()
                        if (pending.isNotEmpty()) {
                            pending.forEach { verifyWithBackend(it) }
                            _uiState.update { it.copy(isPurchasing = false) }
                        } else {
                            _uiState.update {
                                it.copy(isPurchasing = false, error = "ALREADY_OWNED")
                            }
                        }
                    }
                }
                is PurchaseResult.Error -> {
                    _uiState.update {
                        it.copy(isPurchasing = false, error = "PURCHASE_FAILED")
                    }
                }
            }
        }
    }

    fun retry() {
        sessionManager.clearSubscriptionExpired()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
