package com.filemanager.app.domain.repository

import com.filemanager.app.domain.model.FileItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing recently accessed files.
 */
interface RecentFileRepository {
    fun getRecentFiles(limit: Int = 50): Flow<List<FileItem>>
    suspend fun addRecentFile(fileItem: FileItem)
    suspend fun removeRecentFile(path: String)
    suspend fun clearRecentFiles()
}
