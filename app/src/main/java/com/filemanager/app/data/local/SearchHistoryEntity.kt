package com.filemanager.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a search history entry.
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val timestamp: Long
)
