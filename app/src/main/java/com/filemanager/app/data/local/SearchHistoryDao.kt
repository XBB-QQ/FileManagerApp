package com.filemanager.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing search history.
 */
@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 50")
    fun getSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSearchHistory(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun removeSearchHistory(query: String)
}
