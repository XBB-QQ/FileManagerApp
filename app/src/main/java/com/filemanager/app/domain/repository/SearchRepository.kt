package com.filemanager.app.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for search history.
 */
interface SearchRepository {
    fun getSearchHistory(): Flow<List<SearchHistory>>
    suspend fun addSearchHistory(query: String)
    suspend fun clearSearchHistory()
    suspend fun removeSearchHistory(query: String)
}

data class SearchHistory(
    val id: Long = 0,
    val query: String,
    val timestamp: Long
)
