package com.financasdacasa.app.data.repository

import com.financasdacasa.app.data.api.TransactionApi
import com.financasdacasa.app.data.local.dao.TransactionDao
import com.financasdacasa.app.data.local.dao.TransactionSummaryDao
import com.financasdacasa.app.data.local.mapper.toDomain
import com.financasdacasa.app.data.local.mapper.toEntity
import com.financasdacasa.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val api: TransactionApi,
    private val transactionDao: TransactionDao,
    private val summaryDao: TransactionSummaryDao,
) {
    suspend fun list(houseId: String, month: Int, year: Int, search: String? = null, categoryId: String? = null): List<Transaction> {
        // Only cache unfiltered lists
        if (search != null || categoryId != null) return api.list(houseId, month, year, search, categoryId)

        return try {
            val fresh = api.list(houseId, month, year)
            transactionDao.replaceByHouseMonthYear(houseId, month, year, fresh.map { it.toEntity(month, year) })
            fresh
        } catch (e: Exception) {
            val cached = transactionDao.getByHouseMonthYear(houseId, month, year)
            if (cached.isNotEmpty()) cached.map { it.toDomain() }
            else throw e
        }
    }

    suspend fun create(request: CreateTransactionRequest): Transaction =
        api.create(request)

    suspend fun update(id: String, request: UpdateTransactionRequest): Transaction =
        api.update(id, request)

    suspend fun delete(id: String) = api.delete(id)

    suspend fun getSummary(houseId: String, month: Int, year: Int): TransactionSummary {
        return try {
            val fresh = api.getSummary(houseId, month, year)
            summaryDao.insert(fresh.toEntity(houseId))
            fresh
        } catch (e: Exception) {
            val cached = summaryDao.get(houseId, month, year)
            if (cached != null) cached.toDomain()
            else throw e
        }
    }

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
