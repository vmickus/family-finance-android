package com.financasdacasa.app.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.BudgetLimit
import com.financasdacasa.app.data.model.Category
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.data.model.UpsertBudgetLimitRequest
import com.financasdacasa.app.data.repository.BudgetLimitRepository
import com.financasdacasa.app.data.repository.CategoryRepository
import com.financasdacasa.app.data.repository.TransactionRepository
import com.financasdacasa.app.util.evaluateExpression
import com.financasdacasa.app.util.normalizeExpressionInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BudgetsUiState(
    val month: Int = LocalDate.now().monthValue,
    val year: Int = LocalDate.now().year,
    val limits: List<BudgetLimit> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Add form
    val showAddForm: Boolean = false,
    val selectedCategoryId: String = "",
    val amountExpression: String = "",
    val isSaving: Boolean = false,
    // Delete
    val deletingLimitId: String? = null,
)

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetLimitRepository: BudgetLimitRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetsUiState())
    val uiState: StateFlow<BudgetsUiState> = _uiState.asStateFlow()

    private val houseId: String? get() = sessionManager.getSelectedHouseId()

    init {
        loadData()
    }

    fun loadData() {
        val id = houseId ?: return
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.value = s.copy(isLoading = true, error = null)
            try {
                val limits = budgetLimitRepository.list(id)
                val transactions = transactionRepository.list(id, s.month, s.year)
                val categories = categoryRepository.list(id, "expense")
                _uiState.value = _uiState.value.copy(
                    limits = limits,
                    transactions = transactions,
                    expenseCategories = categories,
                    isLoading = false,
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "LOAD_FAILED")
            }
        }
    }

    fun previousMonth() {
        val s = _uiState.value
        val date = LocalDate.of(s.year, s.month, 1).minusMonths(1)
        _uiState.value = s.copy(month = date.monthValue, year = date.year)
        loadData()
    }

    fun nextMonth() {
        val s = _uiState.value
        val date = LocalDate.of(s.year, s.month, 1).plusMonths(1)
        _uiState.value = s.copy(month = date.monthValue, year = date.year)
        loadData()
    }

    val isCurrentMonth: Boolean
        get() {
            val now = LocalDate.now()
            val s = _uiState.value
            return s.month == now.monthValue && s.year == now.year
        }

    // --- Add form ---

    fun showAddForm() {
        _uiState.value = _uiState.value.copy(
            showAddForm = true,
            selectedCategoryId = "",
            amountExpression = "",
        )
    }

    fun dismissAddForm() {
        _uiState.value = _uiState.value.copy(showAddForm = false)
    }

    fun onCategorySelect(categoryId: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun onAmountChange(raw: String) {
        _uiState.value = _uiState.value.copy(amountExpression = normalizeExpressionInput(raw))
    }

    fun saveBudgetLimit() {
        val s = _uiState.value
        val id = houseId ?: return
        val amount = evaluateExpression(s.amountExpression)?.let { Math.round(it * 100) / 100.0 } ?: 0.0
        if (s.selectedCategoryId.isEmpty() || amount <= 0) return

        _uiState.value = s.copy(isSaving = true)
        viewModelScope.launch {
            try {
                budgetLimitRepository.upsert(
                    UpsertBudgetLimitRequest(
                        houseId = id,
                        categoryId = s.selectedCategoryId,
                        monthlyLimit = amount,
                    ),
                )
                _uiState.value = _uiState.value.copy(showAddForm = false, isSaving = false)
                loadData()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = "SAVE_FAILED")
            }
        }
    }

    // --- Delete ---

    fun showDeleteConfirm(limitId: String) {
        _uiState.value = _uiState.value.copy(deletingLimitId = limitId)
    }

    fun dismissDelete() {
        _uiState.value = _uiState.value.copy(deletingLimitId = null)
    }

    fun deleteBudgetLimit() {
        val limitId = _uiState.value.deletingLimitId ?: return
        viewModelScope.launch {
            try {
                budgetLimitRepository.delete(limitId)
                _uiState.value = _uiState.value.copy(deletingLimitId = null)
                loadData()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(deletingLimitId = null, error = "DELETE_FAILED")
            }
        }
    }
}
