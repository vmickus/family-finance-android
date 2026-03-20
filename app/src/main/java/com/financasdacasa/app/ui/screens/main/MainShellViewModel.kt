package com.financasdacasa.app.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.AuthState
import com.financasdacasa.app.data.local.NetworkMonitor
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.User
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

@HiltViewModel
class MainShellViewModel @Inject constructor(
    networkMonitor: NetworkMonitor,
    private val sessionManager: SessionManager,
    private val houseRepository: HouseRepository,
) : ViewModel() {
    val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
        .map { !it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), !networkMonitor.isOnline.value)

    val currentUser: StateFlow<User?> = sessionManager.authState
        .map { (it as? AuthState.Authenticated)?.user }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _houseName = MutableStateFlow<String?>(null)
    val houseName: StateFlow<String?> = _houseName.asStateFlow()

    init {
        loadHouseName()
    }

    private fun loadHouseName() {
        viewModelScope.launch {
            try {
                val houseId = sessionManager.getSelectedHouseId() ?: return@launch
                val houses = houseRepository.list()
                _houseName.value = houses.firstOrNull { it.id == houseId }?.name
            } catch (_: Exception) {
                // house name is non-critical, silently ignore
            }
        }
    }
}
