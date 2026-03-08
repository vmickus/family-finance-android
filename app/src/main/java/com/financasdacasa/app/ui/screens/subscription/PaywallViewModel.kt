package com.financasdacasa.app.ui.screens.subscription

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.BuildConfig
import com.financasdacasa.app.data.billing.BillingManager
import com.financasdacasa.app.data.billing.ProductInfo
import com.financasdacasa.app.data.billing.PurchaseResult
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val products = billingManager.queryProducts()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                billingAvailable = products.isNotEmpty(),
                monthlyProduct = products[BuildConfig.GP_MONTHLY_PRODUCT_ID],
                annualProduct = products[BuildConfig.GP_ANNUAL_PRODUCT_ID],
            )
        }
    }

    fun purchase(activity: Activity, plan: String) {
        _uiState.value = _uiState.value.copy(isPurchasing = true, error = null)

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
                            _uiState.value = _uiState.value.copy(
                                isPurchasing = false,
                                purchaseSuccess = true,
                            )
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                isPurchasing = false,
                                error = "VERIFICATION_FAILED",
                            )
                        }
                    }
                }
                is PurchaseResult.Cancelled -> {
                    _uiState.value = _uiState.value.copy(isPurchasing = false)
                }
                is PurchaseResult.AlreadyOwned -> {
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        error = "ALREADY_OWNED",
                    )
                }
                is PurchaseResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        error = "PURCHASE_FAILED",
                    )
                }
            }
        }
    }

    fun retry() {
        sessionManager.clearSubscriptionExpired()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
