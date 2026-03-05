package com.financasdacasa.app.ui.screens.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.House
import com.financasdacasa.app.data.repository.HouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HouseSelectionUiState(
    val houses: List<House> = emptyList(),
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val newHouseName: String = "",
    val showCreateDialog: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class HouseSelectionViewModel @Inject constructor(
    private val houseRepository: HouseRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HouseSelectionUiState())
    val uiState: StateFlow<HouseSelectionUiState> = _uiState.asStateFlow()

    init {
        loadHouses()
    }

    fun loadHouses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val houses = houseRepository.list()
                _uiState.value = _uiState.value.copy(houses = houses, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "LOAD_FAILED")
            }
        }
    }

    fun selectHouse(house: House) {
        sessionManager.selectHouse(house.id)
    }

    fun onNewHouseNameChange(value: String) {
        _uiState.value = _uiState.value.copy(newHouseName = value)
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true, newHouseName = "")
    }

    fun dismissCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun createHouse() {
        val name = _uiState.value.newHouseName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true)
            try {
                val house = houseRepository.create(name)
                sessionManager.selectHouse(house.id)
                _uiState.value = _uiState.value.copy(
                    houses = _uiState.value.houses + house,
                    showCreateDialog = false,
                    isCreating = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCreating = false, error = "CREATE_FAILED")
            }
        }
    }

    fun logout() {
        sessionManager.logout()
    }
}
