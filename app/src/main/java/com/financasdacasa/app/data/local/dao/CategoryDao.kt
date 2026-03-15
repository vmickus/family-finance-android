package com.financasdacasa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.financasdacasa.app.data.local.entity.CachedCategory

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE houseId = :houseId ORDER BY position ASC, name ASC")
    suspend fun getByHouse(houseId: String): List<CachedCategory>

    @Query("SELECT * FROM categories WHERE houseId = :houseId AND type = :type ORDER BY position ASC, name ASC")
    suspend fun getByHouseAndType(houseId: String, type: String): List<CachedCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CachedCategory>)

    @Query("DELETE FROM categories WHERE houseId = :houseId")
    suspend fun deleteByHouse(houseId: String)

    @Transaction
    suspend fun replaceByHouse(houseId: String, categories: List<CachedCategory>) {
        deleteByHouse(houseId)
        insertAll(categories)
    }

    @Transaction
    suspend fun replaceByHouseAndType(houseId: String, type: String, categories: List<CachedCategory>) {
        deleteByHouseAndType(houseId, type)
        insertAll(categories)
    }

    @Query("DELETE FROM categories WHERE houseId = :houseId AND type = :type")
    suspend fun deleteByHouseAndType(houseId: String, type: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
