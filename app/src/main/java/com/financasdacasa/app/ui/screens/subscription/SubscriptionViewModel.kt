package com.financasdacasa.app.ui.screens.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.model.PaymentEvent
import com.financasdacasa.app.data.model.SubscriptionStatusResponse
import com.financasdacasa.app.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val isLoading: Boolean = true,
    val status: SubscriptionStatusResponse? = null,
    val history: List<PaymentEvent> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val status = subscriptionRepository.getStatus()
                val history = try {
                    subscriptionRepository.getHistory()
                } catch (_: Exception) {
                    emptyList()
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    status = status,
                    history = history,
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "LOAD_FAILED",
                )
            }
        }
    }
}
