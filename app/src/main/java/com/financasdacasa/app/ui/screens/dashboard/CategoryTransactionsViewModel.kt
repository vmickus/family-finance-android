package com.financasdacasa.app.ui.screens.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.AnnualReport
import com.financasdacasa.app.data.model.Category
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.data.model.TransactionSummary
import com.financasdacasa.app.data.repository.CategoryRepository
import com.financasdacasa.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CategoryTransactionsUiState(
    val categoryId: String = "",
    val category: Category? = null,
    val month: Int = LocalDate.now().monthValue,
    val year: Int = LocalDate.now().year,
    val viewMode: DashboardViewMode = DashboardViewMode.MONTHLY,
    val transactions: List<Transaction> = emptyList(),
    val summary: TransactionSummary? = null,
    val annualReport: AnnualReport? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class CategoryTransactionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryTransactionsUiState())
    val uiState: StateFlow<CategoryTransactionsUiState> = _uiState.asStateFlow()

    private val houseId: String? get() = sessionManager.getSelectedHouseId()

    init {
        val categoryId = savedStateHandle.get<String>("categoryId") ?: ""
        _uiState.value = _uiState.value.copy(categoryId = categoryId)
        loadCategory()
        loadData()
    }

    private fun loadCategory() {
        val id = houseId ?: return
        val categoryId = _uiState.value.categoryId
        viewModelScope.launch {
            try {
                val categories = categoryRepository.list(id)
                val category = categories.find { it.id == categoryId }
                _uiState.value = _uiState.value.copy(category = category)
            } catch (_: Exception) { }
        }
    }

    fun loadData() {
        val id = houseId ?: return
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.value = s.copy(isLoading = true, error = null)
            try {
                if (s.viewMode == DashboardViewMode.MONTHLY) {
                    val txDeferred = async {
                        transactionRepository.list(id, s.month, s.year, categoryId = s.categoryId)
                    }
                    val summaryDeferred = async {
                        transactionRepository.getSummary(id, s.month, s.year)
                    }
                    val transactions = txDeferred.await()
                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        summary = summaryDeferred.await(),
                        isLoading = false,
                    )
                } else {
                    val report = transactionRepository.getAnnualReport(
                        id, s.year, categoryId = s.categoryId,
                    )
                    _uiState.value = _uiState.value.copy(
                        annualReport = report,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "LOAD_FAILED")
            }
        }
    }

    fun previousPeriod() {
        val s = _uiState.value
        if (s.viewMode == DashboardViewMode.ANNUAL) {
            _uiState.value = s.copy(year = s.year - 1)
        } else {
            val date = LocalDate.of(s.year, s.month, 1).minusMonths(1)
            _uiState.value = s.copy(month = date.monthValue, year = date.year)
        }
        loadData()
    }

    fun nextPeriod() {
        val s = _uiState.value
        if (s.viewMode == DashboardViewMode.ANNUAL) {
            _uiState.value = s.copy(year = s.year + 1)
        } else {
            val date = LocalDate.of(s.year, s.month, 1).plusMonths(1)
            _uiState.value = s.copy(month = date.monthValue, year = date.year)
        }
        loadData()
    }

    fun toggleViewMode() {
        val newMode = if (_uiState.value.viewMode == DashboardViewMode.MONTHLY) {
            DashboardViewMode.ANNUAL
        } else {
            DashboardViewMode.MONTHLY
        }
        _uiState.value = _uiState.value.copy(viewMode = newMode)
        loadData()
    }
}
