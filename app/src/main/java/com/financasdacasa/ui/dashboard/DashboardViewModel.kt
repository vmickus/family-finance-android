package com.financasdacasa.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.data.model.*
import com.financasdacasa.data.repository.AuthRepository
import com.financasdacasa.data.repository.FinancasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val family: Family? = null,
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val summary: TransactionSummary? = null,
    val month: Int = LocalDate.now().monthValue,
    val year: Int = LocalDate.now().year,
    val showTransactionForm: Boolean = false,
    val showFamilySetup: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val financasRepository: FinancasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.selectedFamilyId.distinctUntilChanged().collect { familyId ->
                if (familyId != null) {
                    loadFamily(familyId, allowFallback = true)
                } else {
                    loadUserFamiliesAndSelect()
                }
            }
        }
    }

    private fun loadUserFamiliesAndSelect() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showFamilySetup = false, error = null) }
            financasRepository.listFamilies()
                .onSuccess { families ->
                    if (families.isNotEmpty()) {
                        val selectedFamily = families.first()
                        authRepository.saveSelectedFamily(selectedFamily.id)
                        loadFamily(selectedFamily.id, allowFallback = false)
                    } else {
                        _uiState.update { it.copy(isLoading = false, showFamilySetup = true) }
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, showFamilySetup = true) }
                }
        }
    }

    private fun loadFamily(familyId: String, allowFallback: Boolean = false) {
        viewModelScope.launch {
            financasRepository.getFamily(familyId)
                .onSuccess { family ->
                    _uiState.update { it.copy(family = family, showFamilySetup = false) }
                    loadData(familyId)
                }
                .onFailure {
                    if (allowFallback) {
                        loadUserFamiliesAndSelect()
                    } else {
                        _uiState.update { it.copy(isLoading = false, showFamilySetup = true) }
                    }
                }
        }
    }

    fun createFamily(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            financasRepository.createFamily(name)
                .onSuccess { family ->
                    authRepository.saveSelectedFamily(family.id)
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            family = family, 
                            showFamilySetup = false
                        ) 
                    }
                    loadData(family.id)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun joinFamily(familyId: String, token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            financasRepository.joinFamily(familyId, token)
                .onSuccess {
                    authRepository.saveSelectedFamily(familyId)
                    loadFamily(familyId)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    private fun loadData(familyId: String) {
        val month = _uiState.value.month
        val year = _uiState.value.year

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load categories
            financasRepository.getCategories(familyId)
                .onSuccess { categories ->
                    _uiState.update { it.copy(categories = categories) }
                }

            // Load transactions
            financasRepository.getTransactions(familyId, month, year)
                .onSuccess { transactions ->
                    _uiState.update { it.copy(transactions = transactions) }
                }

            // Load summary
            financasRepository.getTransactionSummary(familyId, month, year)
                .onSuccess { summary ->
                    _uiState.update { it.copy(summary = summary) }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun changeMonth(month: Int, year: Int) {
        _uiState.update { it.copy(month = month, year = year) }
        _uiState.value.family?.let { loadData(it.id) }
    }

    fun previousMonth() {
        val current = _uiState.value
        if (current.month == 1) {
            changeMonth(12, current.year - 1)
        } else {
            changeMonth(current.month - 1, current.year)
        }
    }

    fun nextMonth() {
        val current = _uiState.value
        if (current.month == 12) {
            changeMonth(1, current.year + 1)
        } else {
            changeMonth(current.month + 1, current.year)
        }
    }

    fun showTransactionForm() {
        _uiState.update { it.copy(showTransactionForm = true) }
    }

    fun hideTransactionForm() {
        _uiState.update { it.copy(showTransactionForm = false) }
    }

    fun createTransaction(
        categoryId: String,
        type: String,
        amount: Double,
        description: String,
        transactionDate: String
    ) {
        val familyId = _uiState.value.family?.id ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            financasRepository.createTransaction(
                familyId = familyId,
                categoryId = categoryId,
                type = type,
                amount = amount,
                description = description,
                transactionDate = transactionDate
            ).onSuccess {
                _uiState.update { it.copy(showTransactionForm = false) }
                loadData(familyId)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createCategory(name: String, color: String) {
        val familyId = _uiState.value.family?.id ?: return

        viewModelScope.launch {
            financasRepository.createCategory(familyId, name, color)
                .onSuccess { category ->
                    _uiState.update { 
                        it.copy(categories = it.categories + category) 
                    }
                }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            financasRepository.deleteTransaction(id)
                .onSuccess {
                    _uiState.value.family?.let { loadData(it.id) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        _uiState.value.family?.let { loadData(it.id) }
    }
}
