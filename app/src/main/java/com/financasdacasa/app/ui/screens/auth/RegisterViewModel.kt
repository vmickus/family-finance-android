package com.financasdacasa.app.ui.screens.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.repository.AuthRepository
import com.financasdacasa.app.util.apiErrorCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val inviteToken: String? = savedStateHandle["inviteToken"]

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) { _uiState.value = _uiState.value.copy(name = value, error = null) }
    fun onEmailChange(value: String) { _uiState.value = _uiState.value.copy(email = value, error = null) }
    fun onPasswordChange(value: String) { _uiState.value = _uiState.value.copy(password = value, error = null) }
    fun onConfirmPasswordChange(value: String) { _uiState.value = _uiState.value.copy(confirmPassword = value, error = null) }

    fun register() {
        val s = _uiState.value
        when {
            s.name.length < 2 -> { _uiState.value = s.copy(error = "NAME_TOO_SHORT"); return }
            s.email.isBlank() -> { _uiState.value = s.copy(error = "EMAIL_REQUIRED"); return }
            s.password.length < 8 -> { _uiState.value = s.copy(error = "PASSWORD_TOO_SHORT"); return }
            !s.password.any { it.isLetter() } || !s.password.any { it.isDigit() } -> {
                _uiState.value = s.copy(error = "PASSWORD_WEAK"); return
            }
            s.password != s.confirmPassword -> { _uiState.value = s.copy(error = "PASSWORD_MISMATCH"); return }
        }

        viewModelScope.launch {
            _uiState.value = s.copy(isLoading = true, error = null)
            try {
                val response = authRepository.register(s.name.trim(), s.email.trim(), s.password, inviteToken)
                sessionManager.login(response.token, response.user)
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.apiErrorCode() ?: "UNKNOWN_ERROR",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "NETWORK_ERROR")
            }
        }
    }

    fun onGoogleToken(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = authRepository.googleLogin(idToken)
                sessionManager.login(response.token, response.user)
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.apiErrorCode() ?: "UNKNOWN_ERROR")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "NETWORK_ERROR")
            }
        }
    }
}
