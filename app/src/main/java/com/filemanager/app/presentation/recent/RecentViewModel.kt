package com.filemanager.app.presentation.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.repository.RecentFileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for recent files.
 */
@HiltViewModel
class RecentViewModel @Inject constructor(
    private val repository: RecentFileRepository
) : ViewModel() {

    val recentFiles: StateFlow<List<FileItem>> = repository.getRecentFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearRecent() {
        viewModelScope.launch {
            repository.clearRecentFiles()
        }
    }
}
