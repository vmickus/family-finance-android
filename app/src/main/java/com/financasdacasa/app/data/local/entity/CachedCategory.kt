package com.financasdacasa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CachedCategory(
    @PrimaryKey val id: String,
    val houseId: String,
    val name: String,
    val color: String,
    val icon: String,
    val type: String,
    val position: Int,
    val seedKey: String?,
    val isSystem: Boolean,
    val isNameOverridden: Boolean,
)
