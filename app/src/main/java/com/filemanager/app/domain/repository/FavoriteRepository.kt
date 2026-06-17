package com.filemanager.app.domain.repository

import com.filemanager.app.domain.model.FileItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing favorite files/folders.
 */
interface FavoriteRepository {
    fun getFavorites(): Flow<List<FileItem>>
    suspend fun addFavorite(fileItem: FileItem)
    suspend fun removeFavorite(path: String)
    suspend fun isFavorite(path: String): Boolean
    suspend fun clearAll()
}
