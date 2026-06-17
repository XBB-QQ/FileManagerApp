package com.filemanager.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing favorite files/folders.
 */
@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE path = :path")
    suspend fun removeFavorite(path: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) > 0 FROM favorites WHERE path = :path")
    suspend fun isFavorite(path: String): Boolean
}
