package com.financasdacasa.app.ui.screens.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.AuthState
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrivacyDataUiState(
    val isExporting: Boolean = false,
    val snackbar: String? = null,
    val error: String? = null,
)

@HiltViewModel
class PrivacyDataViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacyDataUiState())
    val uiState: StateFlow<PrivacyDataUiState> = _uiState.asStateFlow()

    val userEmail: StateFlow<String?> = sessionManager.authState
        .map { (it as? AuthState.Authenticated)?.user?.email }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun requestExport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, error = null)
            try {
                authRepository.requestExport()
                _uiState.value = _uiState.value.copy(isExporting = false, snackbar = "EXPORT_SUCCESS")
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, error = "EXPORT_FAILED")
            }
        }
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbar = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
