package com.financasdacasa.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.Category
import com.financasdacasa.app.data.model.CreateRecurringTransactionRequest
import com.financasdacasa.app.data.model.CreateTransactionRequest
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.data.model.UpdateTransactionRequest
import com.financasdacasa.app.data.repository.CategoryRepository
import com.financasdacasa.app.data.repository.RecurringTransactionRepository
import com.financasdacasa.app.data.repository.TransactionRepository
import com.financasdacasa.app.util.evaluateExpression
import com.financasdacasa.app.util.getTodayLocalDate
import com.financasdacasa.app.util.normalizeExpressionInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionFormState(
    val type: String = "expense",
    val amountExpression: String = "",
    val date: String = getTodayLocalDate(),
    val categoryId: String? = null,
    val description: String = "",
    val isRecurring: Boolean = false,
    val occurrences: Int = 12,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showCategoryPicker: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val recurringRepository: RecurringTransactionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionFormState())
    val state: StateFlow<TransactionFormState> = _state.asStateFlow()

    private val houseId: String? get() = sessionManager.getSelectedHouseId()

    fun getEvaluatedAmount(): Double? = evaluateExpression(_state.value.amountExpression)

    fun initialize(editTransaction: Transaction?) {
        if (editTransaction != null) {
            val amountStr = try {
                editTransaction.amount.toBigDecimal().toPlainString()
            } catch (_: Exception) {
                ""
            }
            _state.value = TransactionFormState(
                type = editTransaction.type,
                amountExpression = amountStr,
                date = editTransaction.transactionDate,
                categoryId = editTransaction.categoryId,
                description = editTransaction.description,
            )
        } else {
            _state.value = TransactionFormState()
        }
        loadCategories()
    }

    private fun loadCategories() {
        val id = houseId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val categories = categoryRepository.list(id)
                _state.value = _state.value.copy(
                    categories = categories,
                    isLoading = false,
                )
            } catch (_: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun onTypeChange(type: String) {
        _state.value = _state.value.copy(
            type = type,
            categoryId = null,
            error = null,
        )
    }

    fun onAmountChange(raw: String) {
        val normalized = normalizeExpressionInput(raw)
        _state.value = _state.value.copy(amountExpression = normalized, error = null)
    }

    fun onDateChange(date: String) {
        _state.value = _state.value.copy(date = date, error = null)
    }

    fun onCategorySelect(categoryId: String) {
        _state.value = _state.value.copy(
            categoryId = categoryId,
            showCategoryPicker = false,
            error = null,
        )
    }

    fun onDescriptionChange(value: String) {
        if (value.length <= 50) {
            _state.value = _state.value.copy(description = value, error = null)
        }
    }

    fun onRecurringToggle(enabled: Boolean) {
        _state.value = _state.value.copy(isRecurring = enabled)
    }

    fun onOccurrencesChange(value: Int) {
        _state.value = _state.value.copy(occurrences = value.coerceIn(2, 12))
    }

    fun showCategoryPicker() {
        _state.value = _state.value.copy(showCategoryPicker = true)
    }

    fun dismissCategoryPicker() {
        _state.value = _state.value.copy(showCategoryPicker = false)
    }

    @Suppress("ReturnCount")
    fun save(editTransactionId: String?, onSuccess: () -> Unit) {
        val s = _state.value
        val id = houseId ?: return

        when {
            s.categoryId == null -> {
                _state.value = s.copy(error = "CATEGORY_REQUIRED")
                return
            }
            evaluateExpression(s.amountExpression).let { it == null || it <= 0 } -> {
                _state.value = s.copy(error = "AMOUNT_REQUIRED")
                return
            }
            s.description.isBlank() -> {
                _state.value = s.copy(error = "DESCRIPTION_REQUIRED")
                return
            }
            s.description.length > 50 -> {
                _state.value = s.copy(error = "DESCRIPTION_TOO_LONG")
                return
            }
        }

        val evaluated = evaluateExpression(s.amountExpression) ?: return
        val amount = Math.round(evaluated * 100) / 100.0

        viewModelScope.launch {
            _state.value = s.copy(isSaving = true, error = null)
            try {
                if (editTransactionId != null) {
                    transactionRepository.update(
                        editTransactionId,
                        UpdateTransactionRequest(
                            categoryId = s.categoryId,
                            type = s.type,
                            amount = amount,
                            description = s.description.trim(),
                            transactionDate = s.date,
                        ),
                    )
                } else if (s.isRecurring) {
                    recurringRepository.create(
                        CreateRecurringTransactionRequest(
                            houseId = id,
                            categoryId = s.categoryId!!,
                            type = s.type,
                            amount = amount,
                            description = s.description.trim(),
                            transactionDate = s.date,
                            occurrences = s.occurrences,
                        ),
                    )
                } else {
                    transactionRepository.create(
                        CreateTransactionRequest(
                            houseId = id,
                            categoryId = s.categoryId!!,
                            type = s.type,
                            amount = amount,
                            description = s.description.trim(),
                            transactionDate = s.date,
                        ),
                    )
                }
                _state.value = _state.value.copy(isSaving = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = "SAVE_FAILED",
                )
            }
        }
    }
}
