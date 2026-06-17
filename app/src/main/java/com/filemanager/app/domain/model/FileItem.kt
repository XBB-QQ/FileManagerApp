package com.filemanager.app.domain.model

import java.util.Date

/**
 * Core model representing a file or directory entry.
 */
data class FileItem(
    val path: String,
    val name: String,
    val type: FileType,
    val size: Long,                // 0 for directories
    val lastModified: Long,        // epoch millis
    val isHidden: Boolean,
    val mimeType: String? = null,
    val childCount: Int? = null    // only for directories: number of children
) {
    val extension: String = if (path.contains('.')) path.substringAfterLast('.', "").lowercase() else ""

    val humanReadableSize: String
        get() = when {
            size < 0 -> "N/A"
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format("%.1f KB", size / 1024.0)
            size < 1024 * 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0))
        }

    val modifiedDate: String
        get() = Date(lastModified).toString()

    companion object {
        fun directory(path: String, name: String = "", childCount: Int? = null): FileItem {
            val displayName = if (name.isBlank()) path.substringAfterLast('/') else name
            return FileItem(
                path = path,
                name = displayName,
                type = FileType.DIRECTORY,
                size = 0,
                lastModified = 0,
                isHidden = displayName.startsWith('.'),
                childCount = childCount
            )
        }
    }
}
