package com.filemanager.app.presentation.collect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the favorites screen.
 */
@HiltViewModel
class CollectViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    /** Live list of favorite files/folders. */
    val favorites: StateFlow<List<FileItem>> = favoriteRepository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Remove a single favorite. */
    fun removeFavorite(path: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(path)
        }
    }

    /** Remove all favorites. */
    fun clearAll() {
        viewModelScope.launch {
            favoriteRepository.clearAll()
        }
    }
}
