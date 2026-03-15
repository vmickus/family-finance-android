package com.financasdacasa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.financasdacasa.app.data.local.entity.CachedGoal

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE houseId = :houseId")
    suspend fun getByHouse(houseId: String): List<CachedGoal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<CachedGoal>)

    @Query("DELETE FROM goals WHERE houseId = :houseId")
    suspend fun deleteByHouse(houseId: String)

    @Transaction
    suspend fun replaceByHouse(houseId: String, goals: List<CachedGoal>) {
        deleteByHouse(houseId)
        insertAll(goals)
    }

    @Query("DELETE FROM goals")
    suspend fun deleteAll()
}
