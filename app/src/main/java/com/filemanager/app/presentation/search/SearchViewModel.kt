package com.filemanager.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.repository.SearchRepository
import com.filemanager.app.domain.repository.FileRepository
import com.filemanager.app.domain.repository.SearchHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for search functionality.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchResults = MutableStateFlow<List<FileItem>>(emptyList())
    val searchResults: StateFlow<List<FileItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<SearchHistory>>(emptyList())
    val searchHistory: StateFlow<List<SearchHistory>> = _searchHistory.asStateFlow()

    init {
        loadSearchHistory()
    }

    fun setQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
        _searchResults.value = emptyList()
    }

    fun startSearch() {
        val q = _query.value.trim()
        if (q.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            searchRepository.addSearchHistory(q)

            fileRepository.searchFiles(q).collect { result ->
                _isSearching.value = false
                result.onSuccess { files ->
                    _searchResults.value = files
                }.onFailure {
                    _searchResults.value = emptyList()
                }
            }
        }
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            searchRepository.getSearchHistory().collect { history ->
                _searchHistory.value = history
            }
        }
    }

    fun removeSearchHistory(queryText: String) {
        viewModelScope.launch {
            searchRepository.removeSearchHistory(queryText)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchRepository.clearSearchHistory()
        }
    }
}
