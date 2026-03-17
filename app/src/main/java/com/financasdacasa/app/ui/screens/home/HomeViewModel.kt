package com.financasdacasa.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.AnnualReport
import com.financasdacasa.app.data.model.FlatAllocationItem
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.data.model.TransactionSummary
import com.financasdacasa.app.data.repository.GoalRepository
import com.financasdacasa.app.data.repository.RecurringTransactionRepository
import com.financasdacasa.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val month: Int = LocalDate.now().monthValue,
    val year: Int = LocalDate.now().year,
    val viewMode: ViewMode = ViewMode.MONTHLY,
    val typeFilter: String? = null,
    val searchInput: String = "",
    val searchFilter: String = "",
    val transactions: List<Transaction> = emptyList(),
    val allocations: List<FlatAllocationItem> = emptyList(),
    val summary: TransactionSummary? = null,
    val annualReport: AnnualReport? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val editingTransaction: Transaction? = null,
    val showTransactionForm: Boolean = false,
)

enum class ViewMode { MONTHLY, ANNUAL }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val recurringRepository: RecurringTransactionRepository,
    private val goalRepository: GoalRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
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
                if (s.viewMode == ViewMode.MONTHLY) {
                    val txDeferred = async {
                        transactionRepository.list(id, s.month, s.year, s.searchFilter.ifBlank { null })
                    }
                    val summaryDeferred = async { transactionRepository.getSummary(id, s.month, s.year) }
                    val allocDeferred = async { goalRepository.listMonthlyAllocations(id, s.month, s.year) }

                    val transactions = txDeferred.await()
                    val summary = summaryDeferred.await()
                    val grouped = allocDeferred.await()

                    val flat = grouped.flatMap { group ->
                        group.items.map { item ->
                            FlatAllocationItem(
                                id = item.id,
                                goalId = item.goalId,
                                goalName = item.goalName,
                                plantType = item.plantType,
                                color = item.color,
                                amount = item.amount,
                                description = item.description,
                                allocationDate = group.allocationDate,
                                userName = group.user?.name,
                            )
                        }
                    }

                    val searchQuery = s.searchFilter.trim()
                    val filteredAllocations = if (searchQuery.isBlank()) {
                        flat
                    } else {
                        val normalized = removeAccents(searchQuery.lowercase())
                        flat.filter { alloc ->
                            removeAccents(alloc.goalName.lowercase()).contains(normalized) ||
                                (alloc.description != null && removeAccents(alloc.description.lowercase()).contains(normalized))
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        allocations = filteredAllocations,
                        summary = summary,
                        isLoading = false,
                    )
                } else {
                    val report = transactionRepository.getAnnualReport(
                        id, s.year, s.typeFilter, s.searchFilter.ifBlank { null },
                    )
                    _uiState.value = _uiState.value.copy(
                        annualReport = report,
                        allocations = emptyList(),
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "LOAD_FAILED",
                )
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

    fun previousYear() {
        _uiState.value = _uiState.value.copy(year = _uiState.value.year - 1)
        loadData()
    }

    fun nextYear() {
        _uiState.value = _uiState.value.copy(year = _uiState.value.year + 1)
        loadData()
    }

    fun toggleViewMode() {
        val newMode = if (_uiState.value.viewMode == ViewMode.MONTHLY) {
            ViewMode.ANNUAL
        } else {
            ViewMode.MONTHLY
        }
        _uiState.value = _uiState.value.copy(viewMode = newMode)
        loadData()
    }

    fun onTypeFilterChange(type: String?) {
        val current = _uiState.value.typeFilter
        _uiState.value = _uiState.value.copy(
            typeFilter = if (current == type) null else type,
        )
    }

    fun onSearchChange(value: String) {
        _uiState.value = _uiState.value.copy(searchInput = value)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.value = _uiState.value.copy(searchFilter = value)
            loadData()
        }
    }

    fun showCreateForm() {
        _uiState.value = _uiState.value.copy(
            showTransactionForm = true,
            editingTransaction = null,
        )
    }

    fun showEditForm(transaction: Transaction) {
        _uiState.value = _uiState.value.copy(
            showTransactionForm = true,
            editingTransaction = transaction,
        )
    }

    fun dismissForm() {
        _uiState.value = _uiState.value.copy(
            showTransactionForm = false,
            editingTransaction = null,
        )
    }

    fun onTransactionSaved() {
        dismissForm()
        loadData()
    }

    fun onTransactionDeleted() {
        loadData()
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                transactionRepository.delete(id)
                loadData()
            } catch (_: Exception) { }
        }
    }

    fun cancelRecurring(recurringId: String, fromDate: String) {
        viewModelScope.launch {
            try {
                recurringRepository.cancelFrom(recurringId, fromDate)
                loadData()
            } catch (_: Exception) { }
        }
    }

    companion object {
        private fun removeAccents(input: String): String =
            Normalizer.normalize(input, Normalizer.Form.NFD)
                .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }
}
