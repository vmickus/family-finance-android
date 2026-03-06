package com.financasdacasa.app.ui.screens.garden

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.*
import com.financasdacasa.app.data.repository.GoalRepository
import com.financasdacasa.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class GoalDetailUiState(
    val goal: Goal? = null,
    val allocations: List<GoalAllocation> = emptyList(),
    val freeBalance: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteConfirm: Boolean = false,
)

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val goalId: String = savedStateHandle["goalId"] ?: ""
    private val houseId: String? get() = sessionManager.getSelectedHouseId()

    private val _uiState = MutableStateFlow(GoalDetailUiState())
    val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val id = houseId ?: return
        val now = LocalDate.now()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val goals = goalRepository.list(id)
                val goal = goals.find { it.id == goalId }
                val allocations = goalRepository.listGoalAllocations(goalId)
                val summary = transactionRepository.getSummary(id, now.monthValue, now.year)
                val monthAllocs = goalRepository.listMonthlyAllocations(id, now.monthValue, now.year)
                val balance = summary.balance.toDoubleOrNull() ?: 0.0
                val totalAllocated = monthAllocs.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }

                _uiState.value = _uiState.value.copy(
                    goal = goal,
                    allocations = allocations,
                    freeBalance = (balance - totalAllocated).coerceAtLeast(0.0),
                    isLoading = false,
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "LOAD_FAILED")
            }
        }
    }

    fun showDeleteConfirm() { _uiState.value = _uiState.value.copy(showDeleteConfirm = true) }
    fun dismissDeleteConfirm() { _uiState.value = _uiState.value.copy(showDeleteConfirm = false) }

    fun deleteGoal(onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                goalRepository.delete(goalId)
                onDeleted()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(showDeleteConfirm = false, error = "DELETE_FAILED")
            }
        }
    }

    fun deleteAllocation(allocationId: String) {
        viewModelScope.launch {
            try {
                goalRepository.deleteAllocation(allocationId)
                loadData()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(error = "DELETE_FAILED")
            }
        }
    }
}
