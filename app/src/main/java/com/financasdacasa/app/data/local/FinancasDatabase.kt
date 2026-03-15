package com.financasdacasa.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.financasdacasa.app.data.local.dao.*
import com.financasdacasa.app.data.local.entity.*

@Database(
    entities = [
        CachedCategory::class,
        CachedHouse::class,
        CachedTransaction::class,
        CachedTransactionSummary::class,
        CachedGoal::class,
        CachedGoalAllocation::class,
        CachedBudgetLimit::class,
        CachedRecurringTransaction::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class FinancasDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun houseDao(): HouseDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionSummaryDao(): TransactionSummaryDao
    abstract fun goalDao(): GoalDao
    abstract fun goalAllocationDao(): GoalAllocationDao
    abstract fun budgetLimitDao(): BudgetLimitDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    suspend fun clearAllCaches() {
        categoryDao().deleteAll()
        houseDao().deleteAll()
        transactionDao().deleteAll()
        transactionSummaryDao().deleteAll()
        goalDao().deleteAll()
        goalAllocationDao().deleteAll()
        budgetLimitDao().deleteAll()
        recurringTransactionDao().deleteAll()
    }
}
