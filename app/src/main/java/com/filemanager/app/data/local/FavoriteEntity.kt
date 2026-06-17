package com.filemanager.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a favorite file/folder.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path: String,
    val name: String,
    val type: String,
    val addedAt: Long
)
