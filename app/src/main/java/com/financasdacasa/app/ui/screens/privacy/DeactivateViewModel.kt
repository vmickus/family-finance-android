package com.financasdacasa.app.ui.screens.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.AuthState
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.OwnedHouseInfo
import com.financasdacasa.app.data.repository.AuthRepository
import com.financasdacasa.app.data.repository.HouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DeactivateStep { WARNING, HOUSES, CONFIRM }

data class DeactivateUiState(
    val step: DeactivateStep = DeactivateStep.WARNING,
    val ownedHouses: List<OwnedHouseInfo> = emptyList(),
    val ownerSelections: Map<String, String> = emptyMap(),
    val password: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val snackbar: String? = null,
)

@HiltViewModel
class DeactivateViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val houseRepository: HouseRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeactivateUiState())
    val uiState: StateFlow<DeactivateUiState> = _uiState.asStateFlow()

    val isGoogleUser: StateFlow<Boolean> = sessionManager.authState
        .map { (it as? AuthState.Authenticated)?.user?.authProvider == "google" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun reset() {
        _uiState.value = DeactivateUiState()
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onOwnerSelected(houseId: String, memberId: String) {
        _uiState.value = _uiState.value.copy(
            ownerSelections = _uiState.value.ownerSelections + (houseId to memberId),
        )
    }

    fun proceedFromWarning() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val check = authRepository.deactivationCheck()
                if (check.housesOwned.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step = DeactivateStep.HOUSES,
                        ownedHouses = check.housesOwned,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step = DeactivateStep.CONFIRM,
                    )
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "DEACTIVATION_CHECK_FAILED")
            }
        }
    }

    fun transferOwnership(houseId: String) {
        val newOwnerId = _uiState.value.ownerSelections[houseId] ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                houseRepository.transferOwnership(houseId, newOwnerId)
                refreshHousesCheck()
                _uiState.value = _uiState.value.copy(snackbar = "TRANSFER_SUCCESS")
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "TRANSFER_FAILED")
            }
        }
    }

    fun deleteHouse(houseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                houseRepository.deleteHouse(houseId)
                refreshHousesCheck()
                _uiState.value = _uiState.value.copy(snackbar = "DELETE_SUCCESS")
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "DELETE_FAILED")
            }
        }
    }

    private suspend fun refreshHousesCheck() {
        val check = authRepository.deactivationCheck()
        if (check.housesOwned.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                step = DeactivateStep.CONFIRM,
                ownedHouses = emptyList(),
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                ownedHouses = check.housesOwned,
            )
        }
    }

    fun deactivateAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val password = _uiState.value.password.ifBlank { null }
                authRepository.deactivateAccount(password)
                sessionManager.logout()
                _uiState.value = _uiState.value.copy(isSubmitting = false, snackbar = "DEACTIVATED")
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "DEACTIVATE_FAILED")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbar = null)
    }
}
