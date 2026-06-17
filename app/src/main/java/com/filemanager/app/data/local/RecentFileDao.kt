package com.filemanager.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing recently accessed files.
 */
@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY lastAccessed DESC LIMIT :limit")
    fun getRecentFiles(limit: Int = 50): Flow<List<RecentFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecentFile(entity: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE path = :path")
    suspend fun removeRecentFile(path: String)

    @Query("DELETE FROM recent_files")
    suspend fun clearRecentFiles()
}
