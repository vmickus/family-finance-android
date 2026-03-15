package com.financasdacasa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "houses")
data class CachedHouse(
    @PrimaryKey val id: String,
    val name: String,
    val ownerId: String,
    val createdAt: String,
)
