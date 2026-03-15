package com.financasdacasa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.financasdacasa.app.data.local.entity.CachedTransaction

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE houseId = :houseId AND month = :month AND year = :year ORDER BY transactionDate DESC, createdAt DESC")
    suspend fun getByHouseMonthYear(houseId: String, month: Int, year: Int): List<CachedTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<CachedTransaction>)

    @Query("DELETE FROM transactions WHERE houseId = :houseId AND month = :month AND year = :year")
    suspend fun deleteByHouseMonthYear(houseId: String, month: Int, year: Int)

    @Transaction
    suspend fun replaceByHouseMonthYear(houseId: String, month: Int, year: Int, transactions: List<CachedTransaction>) {
        deleteByHouseMonthYear(houseId, month, year)
        insertAll(transactions)
    }

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
