package com.financasdacasa.app.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.AuthState
import com.financasdacasa.app.data.local.NetworkMonitor
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.User
import com.financasdacasa.app.data.repository.HouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _houseReady = MutableStateFlow(false)
    val houseReady: StateFlow<Boolean> = _houseReady.asStateFlow()

    private val _navigateToHouseSelection = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToHouseSelection: SharedFlow<Unit> = _navigateToHouseSelection.asSharedFlow()

    init {
        resolveHouse()
    }

    private fun resolveHouse() {
        viewModelScope.launch {
            try {
                val savedId = sessionManager.getSelectedHouseId()
                val houses = houseRepository.list()

                if (savedId != null) {
                    val match = houses.find { it.id == savedId }
                    if (match != null) {
                        _houseName.value = match.name
                        _houseReady.value = true
                        return@launch
                    }
                }

                // No saved house or saved house no longer valid — auto-select if only one
                when {
                    houses.size == 1 -> {
                        sessionManager.selectHouse(houses.first().id)
                        _houseName.value = houses.first().name
                        _houseReady.value = true
                    }
                    houses.isEmpty() -> {
                        // Need to create a house — go to selection screen
                        _navigateToHouseSelection.tryEmit(Unit)
                    }
                    else -> {
                        // Multiple houses — need user to pick
                        _navigateToHouseSelection.tryEmit(Unit)
                    }
                }
            } catch (_: Exception) {
                // Network error — if we have a saved house, try to proceed
                val savedId = sessionManager.getSelectedHouseId()
                if (savedId != null) {
                    _houseReady.value = true
                } else {
                    _navigateToHouseSelection.tryEmit(Unit)
                }
            }
        }
    }
}
