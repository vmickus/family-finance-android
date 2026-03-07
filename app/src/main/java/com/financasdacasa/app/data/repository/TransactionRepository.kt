package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.TransactionApi
import com.financasdacasa.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val api: TransactionApi,
) {
    suspend fun list(houseId: String, month: Int, year: Int, search: String? = null): List<Transaction> =
        api.list(houseId, month, year, search)

    suspend fun create(request: CreateTransactionRequest): Transaction =
        api.create(request)

    suspend fun update(id: String, request: UpdateTransactionRequest): Transaction =
        api.update(id, request)

    suspend fun delete(id: String) = api.delete(id)

    suspend fun getSummary(houseId: String, month: Int, year: Int): TransactionSummary =
        api.getSummary(houseId, month, year)

    suspend fun getAnnualReport(
        houseId: String,
        year: Int,
        type: String? = null,
        search: String? = null,
        categoryId: String? = null,
    ): AnnualReport = api.getAnnualReport(houseId, year, type, search, categoryId)

    suspend fun getYearlySummary(houseId: String, toYear: Int): List<YearlySummary> =
        api.getYearlySummary(houseId, toYear)

    suspend fun getMonthlyHistory(houseId: String, toYear: Int, toMonth: Int): List<MonthlyHistoryEntry> =
        api.getMonthlyHistory(houseId, toYear = toYear, toMonth = toMonth)
}
