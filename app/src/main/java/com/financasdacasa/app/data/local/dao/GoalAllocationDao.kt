package com.financasdacasa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.financasdacasa.app.data.local.entity.CachedGoalAllocation

@Dao
interface GoalAllocationDao {

    @Query("SELECT * FROM goal_allocations WHERE goalId = :goalId ORDER BY allocationDate DESC, createdAt DESC")
    suspend fun getByGoal(goalId: String): List<CachedGoalAllocation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(allocations: List<CachedGoalAllocation>)

    @Query("DELETE FROM goal_allocations WHERE goalId = :goalId")
    suspend fun deleteByGoal(goalId: String)

    @Transaction
    suspend fun replaceByGoal(goalId: String, allocations: List<CachedGoalAllocation>) {
        deleteByGoal(goalId)
        insertAll(allocations)
    }

    @Query("DELETE FROM goal_allocations")
    suspend fun deleteAll()
}
