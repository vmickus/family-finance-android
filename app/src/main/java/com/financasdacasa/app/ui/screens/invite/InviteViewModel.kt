package com.financasdacasa.app.ui.screens.invite

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.AuthState
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.InviteDetails
import com.financasdacasa.app.data.repository.HouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InviteUiState(
    val token: String = "",
    val details: InviteDetails? = null,
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isAccepting: Boolean = false,
    val accepted: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class InviteViewModel @Inject constructor(
    private val houseRepository: HouseRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InviteUiState())
    val uiState: StateFlow<InviteUiState> = _uiState.asStateFlow()

    init {
        val token = savedStateHandle.get<String>("token") ?: ""
        val isAuthenticated = sessionManager.authState.value is AuthState.Authenticated
        _uiState.value = _uiState.value.copy(token = token, isAuthenticated = isAuthenticated)
        if (token.isNotEmpty()) {
            loadDetails(token)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "INVALID")
        }
    }

    private fun loadDetails(token: String) {
        viewModelScope.launch {
            try {
                val details = houseRepository.getInviteDetails(token)
                _uiState.value = _uiState.value.copy(details = details, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "LOAD_FAILED")
            }
        }
    }

    fun acceptInvite() {
        val token = _uiState.value.token
        if (token.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAccepting = true)
            try {
                val house = houseRepository.acceptInvite(token)
                sessionManager.selectHouse(house.id)
                _uiState.value = _uiState.value.copy(isAccepting = false, accepted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isAccepting = false, error = "ACCEPT_FAILED")
            }
        }
    }
}
