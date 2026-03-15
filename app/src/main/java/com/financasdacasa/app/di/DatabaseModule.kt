package com.financasdacasa.app.di

import android.content.Context
import androidx.room.Room
import com.financasdacasa.app.data.local.FinancasDatabase
import com.financasdacasa.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinancasDatabase =
        Room.databaseBuilder(context, FinancasDatabase::class.java, "financas_cache.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCategoryDao(db: FinancasDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideHouseDao(db: FinancasDatabase): HouseDao = db.houseDao()

    @Provides
    fun provideTransactionDao(db: FinancasDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideTransactionSummaryDao(db: FinancasDatabase): TransactionSummaryDao = db.transactionSummaryDao()

    @Provides
    fun provideGoalDao(db: FinancasDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideGoalAllocationDao(db: FinancasDatabase): GoalAllocationDao = db.goalAllocationDao()

    @Provides
    fun provideBudgetLimitDao(db: FinancasDatabase): BudgetLimitDao = db.budgetLimitDao()

    @Provides
    fun provideRecurringTransactionDao(db: FinancasDatabase): RecurringTransactionDao = db.recurringTransactionDao()
}
