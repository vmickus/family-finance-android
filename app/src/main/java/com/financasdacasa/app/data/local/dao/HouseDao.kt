package com.financasdacasa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.financasdacasa.app.data.local.entity.CachedHouse

@Dao
interface HouseDao {

    @Query("SELECT * FROM houses")
    suspend fun getAll(): List<CachedHouse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(houses: List<CachedHouse>)

    @Query("DELETE FROM houses")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(houses: List<CachedHouse>) {
        deleteAll()
        insertAll(houses)
    }
}
