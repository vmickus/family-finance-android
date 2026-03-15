package com.financasdacasa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.financasdacasa.app.data.local.entity.CachedBudgetLimit

@Dao
interface BudgetLimitDao {

    @Query("SELECT * FROM budget_limits WHERE houseId = :houseId")
    suspend fun getByHouse(houseId: String): List<CachedBudgetLimit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(limits: List<CachedBudgetLimit>)

    @Query("DELETE FROM budget_limits WHERE houseId = :houseId")
    suspend fun deleteByHouse(houseId: String)

    @Transaction
    suspend fun replaceByHouse(houseId: String, limits: List<CachedBudgetLimit>) {
        deleteByHouse(houseId)
        insertAll(limits)
    }

    @Query("DELETE FROM budget_limits")
    suspend fun deleteAll()
}
