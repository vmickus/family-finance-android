package com.financasdacasa.app.ui.screens.subscription

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.billing.BillingManager
import com.financasdacasa.app.data.billing.PurchaseResult
import com.financasdacasa.app.data.model.PaymentEvent
import com.financasdacasa.app.data.model.SubscriptionStatusResponse
import com.financasdacasa.app.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val isLoading: Boolean = true,
    val status: SubscriptionStatusResponse? = null,
    val history: List<PaymentEvent> = emptyList(),
    val error: String? = null,
    val cancelDialogVisible: Boolean = false,
    val isCancelling: Boolean = false,
    val isUpgrading: Boolean = false,
    val cancelSuccess: Boolean = false,
    val upgradeCheckoutUrl: String? = null,
    val upgradeError: String? = null,
    val cancelError: String? = null,
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val billingManager: BillingManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val status = subscriptionRepository.getStatus()
                val history = try {
                    subscriptionRepository.getHistory()
                } catch (_: Exception) {
                    emptyList()
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        status = status,
                        history = history,
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "LOAD_FAILED",
                    )
                }
            }
        }
    }

    fun showCancelDialog() {
        _uiState.update { it.copy(cancelDialogVisible = true) }
    }

    fun dismissCancelDialog() {
        _uiState.update { it.copy(cancelDialogVisible = false) }
    }

    fun cancel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, cancelError = null) }
            try {
                subscriptionRepository.cancel()
                _uiState.update {
                    it.copy(
                        isCancelling = false,
                        cancelDialogVisible = false,
                        cancelSuccess = true,
                    )
                }
                loadData()
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isCancelling = false,
                        cancelError = "CANCEL_FAILED",
                    )
                }
            }
        }
    }

    fun clearCancelSuccess() {
        _uiState.update { it.copy(cancelSuccess = false) }
    }

    fun upgradeViaGooglePlay(activity: Activity) {
        _uiState.update { it.copy(isUpgrading = true, upgradeError = null) }

        viewModelScope.launch {
            billingManager.queryProducts()
        }

        billingManager.launchUpgradeFlow(activity) { result ->
            when (result) {
                is PurchaseResult.Success -> {
                    viewModelScope.launch {
                        try {
                            subscriptionRepository.verifyGooglePlay(
                                result.purchaseToken,
                                result.productId,
                            )
                            _uiState.update { it.copy(isUpgrading = false) }
                            loadData()
                        } catch (_: Exception) {
                            _uiState.update {
                                it.copy(isUpgrading = false, upgradeError = "UPGRADE_FAILED")
                            }
                        }
                    }
                }
                is PurchaseResult.Cancelled -> {
                    _uiState.update { it.copy(isUpgrading = false) }
                }
                else -> {
                    _uiState.update {
                        it.copy(isUpgrading = false, upgradeError = "UPGRADE_FAILED")
                    }
                }
            }
        }
    }

    fun upgradeViaMercadoPago() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpgrading = true, upgradeError = null) }
            try {
                val checkoutUrl = subscriptionRepository.checkout("annual")
                _uiState.update {
                    it.copy(isUpgrading = false, upgradeCheckoutUrl = checkoutUrl)
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isUpgrading = false, upgradeError = "UPGRADE_FAILED")
                }
            }
        }
    }

    fun clearUpgradeCheckoutUrl() {
        _uiState.update { it.copy(upgradeCheckoutUrl = null) }
    }

    fun clearUpgradeError() {
        _uiState.update { it.copy(upgradeError = null) }
    }

    fun clearCancelError() {
        _uiState.update { it.copy(cancelError = null) }
    }
}
