package com.filemanager.app.domain.model

/**
 * Represents storage device information (internal, SD card).
 */
data class StorageInfo(
    val name: String,
    val path: String,
    val totalBytes: Long,
    val usableBytes: Long,
    val isRemovable: Boolean = false,
    val isMounted: Boolean = true
) {
    val usedBytes: Long get() = totalBytes - usableBytes

    val humanTotal: String get() = formatSize(totalBytes)
    val humanUsed: String get() = formatSize(usedBytes)
    val humanFree: String get() = formatSize(usableBytes)
    val usagePercent: Float
        get() = if (totalBytes <= 0) 0f else (usedBytes.toFloat() / totalBytes.toFloat()) * 100f

    companion object {
        fun formatSize(bytes: Long): String = when {
            bytes < 1024L -> "$bytes B"
            bytes < 1024L * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024L * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
