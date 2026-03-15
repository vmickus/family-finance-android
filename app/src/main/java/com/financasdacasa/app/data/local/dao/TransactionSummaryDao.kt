package com.financasdacasa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.financasdacasa.app.data.local.entity.CachedTransactionSummary

@Dao
interface TransactionSummaryDao {

    @Query("SELECT * FROM transaction_summaries WHERE houseId = :houseId AND month = :month AND year = :year LIMIT 1")
    suspend fun get(houseId: String, month: Int, year: Int): CachedTransactionSummary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: CachedTransactionSummary)

    @Query("DELETE FROM transaction_summaries")
    suspend fun deleteAll()
}
