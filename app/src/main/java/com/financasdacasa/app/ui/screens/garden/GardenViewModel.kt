package com.financasdacasa.app.ui.screens.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.*
import com.financasdacasa.app.data.repository.GoalRepository
import com.financasdacasa.app.data.repository.TransactionRepository
import com.financasdacasa.app.util.DEFAULT_GOAL_COLOR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class GardenUiState(
    val goals: List<Goal> = emptyList(),
    val monthlyAllocations: List<GroupedAllocation> = emptyList(),
    val balance: Double = 0.0,
    val totalAllocated: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Goal form
    val showGoalForm: Boolean = false,
    val editingGoal: Goal? = null,
    val formName: String = "",
    val formTargetDigits: String = "",
    val formPlantType: String = "tree",
    val formColor: String = DEFAULT_GOAL_COLOR,
    val formPriority: Int = 0,
    val formDeadline: String? = null,
    val formError: String? = null,
    val isSaving: Boolean = false,
    // Water
    val showWaterSheet: Boolean = false,
    val waterAmounts: Map<String, String> = emptyMap(),
    val waterError: String? = null,
    val isWatering: Boolean = false,
    // Delete
    val deletingGoal: Goal? = null,
    // Success message
    val successMessage: String? = null,
)

@HiltViewModel
class GardenViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GardenUiState())
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()

    private val houseId: String? get() = sessionManager.getSelectedHouseId()

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
                val summary = transactionRepository.getSummary(id, now.monthValue, now.year)
                val allocations = goalRepository.listMonthlyAllocations(id, now.monthValue, now.year)
                val balance = summary.balance.toDoubleOrNull() ?: 0.0
                val totalAllocated = allocations.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }
                _uiState.value = _uiState.value.copy(
                    goals = goals.sortedByDescending {
                        val target = it.targetAmount.toDoubleOrNull() ?: 1.0
                        val current = it.currentAmount.toDoubleOrNull() ?: 0.0
                        if (target > 0) current / target else 0.0
                    },
                    monthlyAllocations = allocations,
                    balance = balance,
                    totalAllocated = totalAllocated,
                    isLoading = false,
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "LOAD_FAILED")
            }
        }
    }

    val freeBalance: Double
        get() = (_uiState.value.balance - _uiState.value.totalAllocated).coerceAtLeast(0.0)

    val activeGoals: List<Goal>
        get() = _uiState.value.goals.filter { it.status == "active" }

    val availablePriority: Int
        get() {
            val used = activeGoals
                .filter { it.id != _uiState.value.editingGoal?.id }
                .sumOf { it.priorityPercent }
            return (100 - used).coerceAtLeast(0)
        }

    // --- Goal form ---

    fun showCreateForm() {
        _uiState.value = _uiState.value.copy(
            showGoalForm = true,
            editingGoal = null,
            formName = "",
            formTargetDigits = "",
            formPlantType = "tree",
            formColor = DEFAULT_GOAL_COLOR,
            formPriority = 0,
            formDeadline = null,
            formError = null,
        )
    }

    fun showEditForm(goal: Goal) {
        val targetCents = ((goal.targetAmount.toDoubleOrNull() ?: 0.0) * 100).toLong().toString()
        _uiState.value = _uiState.value.copy(
            showGoalForm = true,
            editingGoal = goal,
            formName = goal.name,
            formTargetDigits = targetCents,
            formPlantType = goal.plantType,
            formColor = goal.color,
            formPriority = goal.priorityPercent,
            formDeadline = goal.deadline,
            formError = null,
        )
    }

    fun dismissGoalForm() {
        _uiState.value = _uiState.value.copy(showGoalForm = false, editingGoal = null)
    }

    fun onFormNameChange(v: String) { _uiState.value = _uiState.value.copy(formName = v, formError = null) }
    fun onFormTargetChange(v: String) { _uiState.value = _uiState.value.copy(formTargetDigits = v.filter { it.isDigit() }, formError = null) }
    fun onFormPlantTypeChange(v: String) { _uiState.value = _uiState.value.copy(formPlantType = v) }
    fun onFormColorChange(v: String) { _uiState.value = _uiState.value.copy(formColor = v) }
    fun onFormPriorityChange(v: Int) { _uiState.value = _uiState.value.copy(formPriority = v.coerceIn(0, availablePriority)) }
    fun onFormDeadlineChange(v: String?) { _uiState.value = _uiState.value.copy(formDeadline = v) }

    fun saveGoal() {
        val s = _uiState.value
        val name = s.formName.trim()
        if (name.isEmpty()) { _uiState.value = s.copy(formError = "NAME_REQUIRED"); return }
        if (name.length > 50) { _uiState.value = s.copy(formError = "NAME_MAX"); return }
        val cents = s.formTargetDigits.toLongOrNull() ?: 0L
        if (cents <= 0 && s.editingGoal == null) { _uiState.value = s.copy(formError = "TARGET_REQUIRED"); return }

        val id = houseId ?: return
        _uiState.value = s.copy(isSaving = true, formError = null)

        viewModelScope.launch {
            try {
                val editing = s.editingGoal
                if (editing != null) {
                    goalRepository.update(editing.id, UpdateGoalRequest(
                        name = name,
                        targetAmount = if (cents > 0) cents / 100.0 else null,
                        plantType = s.formPlantType,
                        color = s.formColor,
                        priorityPercent = s.formPriority,
                        deadline = s.formDeadline,
                    ))
                } else {
                    goalRepository.create(CreateGoalRequest(
                        houseId = id,
                        name = name,
                        targetAmount = cents / 100.0,
                        plantType = s.formPlantType,
                        color = s.formColor,
                        priorityPercent = s.formPriority,
                        deadline = s.formDeadline,
                    ))
                }
                _uiState.value = _uiState.value.copy(showGoalForm = false, editingGoal = null, isSaving = false)
                loadData()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, formError = "SAVE_FAILED")
            }
        }
    }

    fun archiveGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                goalRepository.update(goal.id, UpdateGoalRequest(status = "archived"))
                loadData()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(error = "SAVE_FAILED")
            }
        }
    }

    // --- Delete ---

    fun showDeleteConfirm(goal: Goal) { _uiState.value = _uiState.value.copy(deletingGoal = goal) }
    fun dismissDelete() { _uiState.value = _uiState.value.copy(deletingGoal = null) }

    fun deleteGoal() {
        val goal = _uiState.value.deletingGoal ?: return
        viewModelScope.launch {
            try {
                goalRepository.delete(goal.id)
                _uiState.value = _uiState.value.copy(deletingGoal = null)
                loadData()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(deletingGoal = null, error = "DELETE_FAILED")
            }
        }
    }

    // --- Water ---

    fun showWaterSheet(singleGoalId: String? = null) {
        val goals = if (singleGoalId != null) {
            activeGoals.filter { it.id == singleGoalId }
        } else {
            activeGoals
        }
        val free = freeBalance
        val totalPriority = goals.sumOf { it.priorityPercent }.takeIf { it > 0 } ?: 1

        val amounts = goals.associate { goal ->
            val share = (goal.priorityPercent.toDouble() / totalPriority) * free
            val remaining = (goal.targetAmount.toDoubleOrNull() ?: 0.0) - (goal.currentAmount.toDoubleOrNull() ?: 0.0)
            val amount = minOf(share, remaining.coerceAtLeast(0.0))
            val cents = (amount * 100).toLong().toString()
            goal.id to cents
        }

        _uiState.value = _uiState.value.copy(
            showWaterSheet = true,
            waterAmounts = amounts,
            waterError = null,
        )
    }

    fun dismissWaterSheet() { _uiState.value = _uiState.value.copy(showWaterSheet = false) }

    fun onWaterAmountChange(goalId: String, digits: String) {
        val amounts = _uiState.value.waterAmounts.toMutableMap()
        amounts[goalId] = digits.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(waterAmounts = amounts, waterError = null)
    }

    fun submitWater() {
        val id = houseId ?: return
        val s = _uiState.value
        val allocations = s.waterAmounts.mapNotNull { (goalId, digits) ->
            val cents = digits.toLongOrNull() ?: 0L
            if (cents > 0) AllocationItemRequest(goalId, cents / 100.0) else null
        }
        if (allocations.isEmpty()) return

        val total = allocations.sumOf { it.amount }
        if (total > freeBalance + 0.05) {
            _uiState.value = s.copy(waterError = "EXCEEDS_BALANCE")
            return
        }

        _uiState.value = s.copy(isWatering = true, waterError = null)
        viewModelScope.launch {
            try {
                goalRepository.createAllocations(CreateAllocationRequest(
                    houseId = id,
                    allocationDate = LocalDate.now().toString(),
                    allocations = allocations,
                ))
                _uiState.value = _uiState.value.copy(
                    showWaterSheet = false,
                    isWatering = false,
                    successMessage = "WATERED",
                )
                loadData()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isWatering = false, waterError = "ALLOCATION_FAILED")
            }
        }
    }

    fun clearSuccessMessage() { _uiState.value = _uiState.value.copy(successMessage = null) }
}
