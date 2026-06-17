package com.filemanager.app.data.repository

import com.filemanager.app.data.local.FileManagerDatabase
import com.filemanager.app.data.local.SearchHistoryEntity
import com.filemanager.app.data.mapper.toDomain
import com.filemanager.app.domain.repository.SearchHistory
import com.filemanager.app.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository implementation for search history using Room.
 */
class SearchRepositoryImpl @Inject constructor(
    private val database: FileManagerDatabase
) : SearchRepository {

    private val dao = database.searchHistoryDao()

    override fun getSearchHistory(): Flow<List<SearchHistory>> =
        dao.getSearchHistory().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addSearchHistory(query: String) {
        dao.addSearchHistory(SearchHistoryEntity(query = query, timestamp = System.currentTimeMillis()))
    }

    override suspend fun clearSearchHistory() {
        dao.clearSearchHistory()
    }

    override suspend fun removeSearchHistory(query: String) {
        dao.removeSearchHistory(query)
    }
}
