package com.filemanager.app.data.mapper

import com.filemanager.app.data.local.FavoriteEntity
import com.filemanager.app.data.local.RecentFileEntity
import com.filemanager.app.data.local.SearchHistoryEntity
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import com.filemanager.app.domain.repository.SearchHistory

/**
 * Convert RecentFileEntity -> FileItem
 */
fun RecentFileEntity.toFileItem(): FileItem {
    return FileItem(
        path = path,
        name = name,
        type = runCatching { FileType.valueOf(type) }.getOrElse { FileType.UNKNOWN },
        size = size,
        lastModified = lastModified,
        isHidden = isHidden,
        mimeType = mimeType
    )
}

/**
 * Convert FileItem -> RecentFileEntity
 */
fun FileItem.toRecentFileEntity(): RecentFileEntity {
    return RecentFileEntity(
        path = path,
        name = name,
        type = type.name,
        size = size,
        lastModified = lastModified,
        lastAccessed = System.currentTimeMillis(),
        isHidden = isHidden,
        mimeType = mimeType
    )
}

/**
 * Convert FavoriteEntity -> FileItem
 */
fun FavoriteEntity.toFileItem(): FileItem {
    return FileItem(
        path = path,
        name = name,
        type = runCatching { FileType.valueOf(type) }.getOrElse { FileType.UNKNOWN },
        size = 0,
        lastModified = 0,
        isHidden = false
    )
}

/**
 * Convert SearchHistoryEntity -> SearchHistory domain model
 */
fun SearchHistoryEntity.toDomain(): SearchHistory {
    return SearchHistory(
        id = id,
        query = query,
        timestamp = timestamp
    )
}
