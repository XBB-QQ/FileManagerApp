package com.filemanager.app.presentation.latest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for "Latest Files" screen — shows files sorted by modification time.
 */
@HiltViewModel
class LatestViewModel @Inject constructor(
    application: Application,
    private val fileRepository: FileRepository
) : AndroidViewModel(application) {

    private val _currentPath = MutableStateFlow("/storage/emulated/0")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _latestFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val latestFiles: StateFlow<List<FileItem>> = _latestFiles.asStateFlow()

    init {
        loadLatestFiles(_currentPath.value)
    }

    fun setPath(path: String) {
        _currentPath.value = path
        loadLatestFiles(path)
    }

    fun refresh() {
        loadLatestFiles(_currentPath.value)
    }

    private fun loadLatestFiles(path: String) {
        viewModelScope.launch {
            fileRepository.listFiles(path, showHidden = false)
                .collect { result ->
                    result.onSuccess { files ->
                        // Sort by modification time, newest first
                        val sorted = files
                            .filter { it.type != com.filemanager.app.domain.model.FileType.DIRECTORY }
                            .sortedByDescending { it.lastModified }
                        _latestFiles.value = sorted
                    }
                }
        }
    }
}
