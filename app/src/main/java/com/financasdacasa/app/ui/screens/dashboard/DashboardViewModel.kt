package com.financasdacasa.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financasdacasa.app.data.local.SessionManager
import com.financasdacasa.app.data.model.*
import com.financasdacasa.app.data.repository.HouseRepository
import com.financasdacasa.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class DashboardViewMode { MONTHLY, ANNUAL }

data class DashboardUiState(
    val month: Int = LocalDate.now().monthValue,
    val year: Int = LocalDate.now().year,
    val viewMode: DashboardViewMode = DashboardViewMode.MONTHLY,
    val summary: TransactionSummary? = null,
    val transactions: List<Transaction> = emptyList(),
    val annualReport: AnnualReport? = null,
    val prevYearReport: AnnualReport? = null,
    val yearlySummary: List<YearlySummary> = emptyList(),
    val monthlyHistory: List<MonthlyHistoryEntry> = emptyList(),
    val members: List<HouseMember> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val houseRepository: HouseRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

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
                    val monthlyHistDeferred = async { transactionRepository.getMonthlyHistory(id, s.year, s.month) }
                    val yearlySumDeferred = async { transactionRepository.getYearlySummary(id, s.year) }
                    val currentReportDeferred = async { transactionRepository.getAnnualReport(id, s.year) }
                    val prevReportDeferred = if (s.month <= 6) {
                        async { transactionRepository.getAnnualReport(id, s.year - 1) }
                    } else null
                    val membersDeferred = async { houseRepository.getMembers(id) }

                    _uiState.value = _uiState.value.copy(
                        transactions = txDeferred.await(),
                        summary = summaryDeferred.await(),
                        monthlyHistory = monthlyHistDeferred.await(),
                        yearlySummary = yearlySumDeferred.await(),
                        annualReport = currentReportDeferred.await(),
                        prevYearReport = prevReportDeferred?.await(),
                        members = membersDeferred.await(),
                        isLoading = false,
                    )
                } else {
                    val reportDeferred = async { transactionRepository.getAnnualReport(id, s.year) }
                    val yearlySumDeferred = async { transactionRepository.getYearlySummary(id, s.year) }
                    val membersDeferred = async { houseRepository.getMembers(id) }

                    _uiState.value = _uiState.value.copy(
                        annualReport = reportDeferred.await(),
                        yearlySummary = yearlySumDeferred.await(),
                        members = membersDeferred.await(),
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
