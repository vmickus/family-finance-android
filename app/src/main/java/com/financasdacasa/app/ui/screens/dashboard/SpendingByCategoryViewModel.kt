package com.financasdacasa.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.AnnualReport
import com.financasdacasa.app.data.model.Transaction
import com.financasdacasa.app.data.model.TransactionSummary
import com.financasdacasa.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpendingByCategoryUiState(
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
class SpendingByCategoryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpendingByCategoryUiState())
    val uiState: StateFlow<SpendingByCategoryUiState> = _uiState.asStateFlow()

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
                if (s.viewMode == DashboardViewMode.MONTHLY) {
                    val txDeferred = async { transactionRepository.list(id, s.month, s.year) }
                    val summaryDeferred = async { transactionRepository.getSummary(id, s.month, s.year) }
                    _uiState.value = _uiState.value.copy(
                        transactions = txDeferred.await(),
                        summary = summaryDeferred.await(),
                        isLoading = false,
                    )
                } else {
                    val report = transactionRepository.getAnnualReport(id, s.year)
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
