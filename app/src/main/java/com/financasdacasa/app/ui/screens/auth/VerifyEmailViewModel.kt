package com.financasdacasa.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyEmailUiState(
    val resendLoading: Boolean = false,
    val resendSuccess: Boolean = false,
    val checkLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyEmailUiState())
    val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()

    fun resendVerification() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(resendLoading = true, resendSuccess = false, error = null)
            try {
                authRepository.resendVerification()
                _uiState.value = _uiState.value.copy(resendLoading = false, resendSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(resendLoading = false, error = "RESEND_FAILED")
            }
        }
    }

    fun checkVerification() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(checkLoading = true, error = null)
            try {
                val user = authRepository.getMe()
                if (user.emailVerified) {
                    sessionManager.updateUser(user)
                } else {
                    _uiState.value = _uiState.value.copy(checkLoading = false, error = "NOT_VERIFIED_YET")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(checkLoading = false, error = "CHECK_FAILED")
            }
        }
    }

    fun logout() {
        sessionManager.logout()
    }
}
