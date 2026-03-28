package com.financasdacasa.app.ui.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.Category
import com.financasdacasa.app.data.model.RecurringTransaction
import com.financasdacasa.app.data.model.UpdateRecurringTransactionRequest
import com.financasdacasa.app.data.repository.CategoryRepository
import com.financasdacasa.app.data.repository.RecurringTransactionRepository
import com.financasdacasa.app.util.evaluateExpression
import com.financasdacasa.app.util.normalizeExpressionInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringUiState(
    val items: List<RecurringTransaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Edit form
    val editingItem: RecurringTransaction? = null,
    val formCategoryId: String = "",
    val formAmountExpression: String = "",
    val formDescription: String = "",
    val isSaving: Boolean = false,
    // Delete
    val deletingItem: RecurringTransaction? = null,
)

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val recurringRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    private val houseId: String? get() = sessionManager.getSelectedHouseId()

    init {
        loadData()
    }

    fun loadData() {
        val id = houseId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val items = recurringRepository.list(id)
                val categories = categoryRepository.list(id)
                val sorted = items.sortedWith(compareByDescending<RecurringTransaction> { it.isActive }.thenByDescending { it.createdAt })
                _uiState.value = _uiState.value.copy(items = sorted, categories = categories, isLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "LOAD_FAILED")
            }
        }
    }

    // --- Edit ---

    fun showEditForm(item: RecurringTransaction) {
        val amount = item.amount.toDoubleOrNull() ?: 0.0
        val formatted = if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString()
        _uiState.value = _uiState.value.copy(
            editingItem = item,
            formCategoryId = item.categoryId ?: "",
            formAmountExpression = formatted,
            formDescription = item.description,
        )
    }

    fun dismissEditForm() {
        _uiState.value = _uiState.value.copy(editingItem = null)
    }

    fun onFormCategoryChange(id: String) { _uiState.value = _uiState.value.copy(formCategoryId = id) }
    fun onFormAmountChange(v: String) { _uiState.value = _uiState.value.copy(formAmountExpression = normalizeExpressionInput(v)) }
    fun onFormDescriptionChange(v: String) { _uiState.value = _uiState.value.copy(formDescription = v) }

    fun saveEdit() {
        val item = _uiState.value.editingItem ?: return
        val amount = evaluateExpression(_uiState.value.formAmountExpression)?.let { Math.round(it * 100) / 100.0 } ?: 0.0
        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            try {
                recurringRepository.update(item.id, UpdateRecurringTransactionRequest(
                    categoryId = _uiState.value.formCategoryId.ifEmpty { null },
                    amount = if (amount > 0) amount else null,
                    description = _uiState.value.formDescription.ifEmpty { null },
                ))
                _uiState.value = _uiState.value.copy(editingItem = null, isSaving = false)
                loadData()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = "SAVE_FAILED")
            }
        }
    }

    // --- Delete ---

    fun showDeleteConfirm(item: RecurringTransaction) { _uiState.value = _uiState.value.copy(deletingItem = item) }
    fun dismissDelete() { _uiState.value = _uiState.value.copy(deletingItem = null) }

    fun deleteRecurring() {
        val item = _uiState.value.deletingItem ?: return
        viewModelScope.launch {
            try {
                recurringRepository.delete(item.id)
                _uiState.value = _uiState.value.copy(deletingItem = null)
                loadData()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(deletingItem = null, error = "DELETE_FAILED")
            }
        }
    }
}
