package com.filemanager.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a recently accessed file.
 */
@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path: String,
    val name: String,
    val type: String,        // FileType enum name
    val size: Long,
    val lastModified: Long,
    val lastAccessed: Long,
    val isHidden: Boolean = false,
    val mimeType: String? = null
)
