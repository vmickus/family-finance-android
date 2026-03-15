package com.financasdacasa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.financasdacasa.app.data.local.entity.CachedRecurringTransaction

@Dao
interface RecurringTransactionDao {

    @Query("SELECT * FROM recurring_transactions WHERE houseId = :houseId ORDER BY createdAt DESC")
    suspend fun getByHouse(houseId: String): List<CachedRecurringTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<CachedRecurringTransaction>)

    @Query("DELETE FROM recurring_transactions WHERE houseId = :houseId")
    suspend fun deleteByHouse(houseId: String)

    @Transaction
    suspend fun replaceByHouse(houseId: String, transactions: List<CachedRecurringTransaction>) {
        deleteByHouse(houseId)
        insertAll(transactions)
    }

    @Query("DELETE FROM recurring_transactions")
    suspend fun deleteAll()
}
