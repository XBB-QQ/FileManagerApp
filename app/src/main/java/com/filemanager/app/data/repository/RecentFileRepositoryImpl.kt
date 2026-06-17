package com.filemanager.app.data.repository

import com.filemanager.app.data.local.FileManagerDatabase
import com.filemanager.app.data.local.RecentFileEntity
import com.filemanager.app.data.mapper.toFileItem
import com.filemanager.app.data.mapper.toRecentFileEntity
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.repository.RecentFileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository implementation for recent files using Room.
 */
class RecentFileRepositoryImpl @Inject constructor(
    private val database: FileManagerDatabase
) : RecentFileRepository {

    private val dao = database.recentFileDao()

    override fun getRecentFiles(limit: Int): Flow<List<FileItem>> =
        dao.getRecentFiles(limit).map { entities ->
            entities.map { it.toFileItem() }
        }

    override suspend fun addRecentFile(fileItem: FileItem) {
        val entity = fileItem.toRecentFileEntity()
        dao.addRecentFile(entity)
    }

    override suspend fun removeRecentFile(path: String) {
        dao.removeRecentFile(path)
    }

    override suspend fun clearRecentFiles() {
        dao.clearRecentFiles()
    }
}
