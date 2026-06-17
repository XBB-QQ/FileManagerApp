package com.filemanager.app.data.repository

import com.filemanager.app.data.local.FavoriteEntity
import com.filemanager.app.data.local.FileManagerDatabase
import com.filemanager.app.data.mapper.toFileItem
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository implementation for favorites using Room.
 */
class FavoriteRepositoryImpl @Inject constructor(
    private val database: FileManagerDatabase
) : FavoriteRepository {

    private val dao = database.favoriteDao()

    override fun getFavorites(): Flow<List<FileItem>> =
        dao.getFavorites().map { entities ->
            entities.map { it.toFileItem() }
        }

    override suspend fun addFavorite(fileItem: FileItem) {
        val entity = FavoriteEntity(
            path = fileItem.path,
            name = fileItem.name,
            type = fileItem.type.name,
            addedAt = System.currentTimeMillis()
        )
        dao.addFavorite(entity)
    }

    override suspend fun removeFavorite(path: String) {
        dao.removeFavorite(path)
    }

    override suspend fun isFavorite(path: String): Boolean {
        return dao.isFavorite(path)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}
