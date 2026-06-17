package com.filemanager.app.presentation.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import com.filemanager.app.domain.model.StorageInfo
import com.filemanager.app.domain.repository.FileRepository
import com.filemanager.app.domain.repository.RecentFileRepository
import com.filemanager.app.domain.repository.FavoriteRepository
import com.filemanager.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * UI state for the main file browser screen.
 */
data class MainUiState(
    val files: List<FileItem> = emptyList(),
    val currentPath: String = "/",
    val isLoading: Boolean = false,
    val sortBy: String = "name",
    val sortOrder: String = "asc",
    val showHidden: Boolean = false,
    val selectedFiles: Set<String> = emptySet(),
    val isMultiSelect: Boolean = false,
    val clipboardFiles: List<String> = emptyList(),
    val clipboardOperation: ClipboardOperation? = null,
    val storages: List<StorageInfo> = emptyList(),
    val errorMessage: String? = null,
    val isCompressing: Boolean = false,
    val isExtracting: Boolean = false,
    val compressMessage: String? = null,
    val extractMessage: String? = null
)

sealed class ClipboardOperation {
    object Cut : ClipboardOperation()
    object Copy : ClipboardOperation()
}

/**
 * ViewModel for the main file browser.
 * Handles browsing files, file operations, and state management.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val fileRepository: FileRepository,
    private val recentFileRepository: RecentFileRepository,
    private val favoriteRepository: FavoriteRepository,
    private val browseFilesUseCase: BrowseFilesUseCase,
    private val copyFilesUseCase: CopyFilesUseCase,
    private val cutFilesUseCase: CutFilesUseCase,
    private val deleteFilesUseCase: DeleteFilesUseCase,
    private val renameFileUseCase: RenameFileUseCase,
    private val createZipUseCase: CreateZipUseCase,
    private val extractZipUseCase: ExtractZipUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _currentLoadPath = MutableStateFlow<String?>(null)

    init {
        _currentLoadPath.value = "/"
        loadStorageInfo()

        viewModelScope.launch {
            _currentLoadPath
                .filterNotNull()
                .distinctUntilChanged()
                .collect { path ->
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    try {
                        val result = fileRepository.listFiles(path, _uiState.value.showHidden).first()
                        val files = result.getOrThrow()
                        _uiState.update {
                            it.copy(
                                files = sortFiles(files),
                                currentPath = path,
                                isLoading = false
                            )
                        }
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message ?: "加载文件失败"
                            )
                        }
                    }
                }
        }
    }

    /**
     * Load files in a directory.
     * Setting the path triggers the collector above, which automatically
     * handles cancellation of any in-flight load via distinctUntilChanged.
     */
    fun loadFiles(path: String) {
        _currentLoadPath.value = path
    }

    /**
     * Load storage information.
     */
    private fun loadStorageInfo() {
        viewModelScope.launch {
            fileRepository.getStorageInfo()
                .collect { result ->
                    result.onSuccess { storages ->
                        _uiState.update { it.copy(storages = storages) }
                    }
                }
        }
    }

    /**
     * Navigate into a directory.
     */
    fun navigateToDirectory(fileItem: FileItem) {
        loadFiles(fileItem.path)
    }

    /**
     * Go up one directory level.
     */
    fun goUp() {
        val currentPath = _uiState.value.currentPath
        val parts = currentPath.split("/").filter { it.isNotEmpty() }
        if (parts.size > 1) {
            val parentPath = "/" + parts.dropLast(1).joinToString("/")
            loadFiles(parentPath)
        } else {
            loadFiles("/")
        }
    }

    /**
     * Navigate to a specific path.
     */
    fun navigateToPath(path: String) {
        loadFiles(path)
    }

    /**
     * Sort files by specified criteria.
     */
    fun setSortBy(sortBy: String) {
        _uiState.update { it.copy(sortBy = sortBy) }
        _uiState.update {
            it.copy(files = sortFiles(it.files))
        }
    }

    /**
     * Toggle sort order.
     */
    fun toggleSortOrder() {
        val newOrder = if (_uiState.value.sortOrder == "asc") "desc" else "asc"
        _uiState.update { it.copy(sortOrder = newOrder) }
        _uiState.update {
            it.copy(files = sortFiles(it.files))
        }
    }

    /**
     * Toggle hidden files visibility.
     */
    fun toggleShowHidden() {
        _uiState.update { it.copy(showHidden = !it.showHidden) }
        loadFiles(_uiState.value.currentPath)
    }

    /**
     * Enter multi-select mode with a file.
     */
    fun enterMultiSelect(fileItem: FileItem) {
        _uiState.update {
            it.copy(
                selectedFiles = setOf(fileItem.path),
                isMultiSelect = true
            )
        }
    }

    /**
     * Toggle selection of a file.
     */
    fun toggleFileSelection(path: String) {
        _uiState.update { state ->
            val newSet = if (path in state.selectedFiles) {
                state.selectedFiles - path
            } else {
                state.selectedFiles + path
            }
            state.copy(selectedFiles = newSet)
        }
    }

    /**
     * Select all files.
     */
    fun selectAll() {
        _uiState.update {
            it.copy(selectedFiles = it.files.map { f -> f.path }.toSet())
        }
    }

    /**
     * Exit multi-select mode.
     */
    fun exitMultiSelect() {
        _uiState.update {
            it.copy(
                selectedFiles = emptySet(),
                isMultiSelect = false
            )
        }
    }

    /**
     * Cut a single file (via context menu).
     */
    fun cutFile(path: String) {
        _uiState.update {
            it.copy(
                clipboardFiles = listOf(path),
                clipboardOperation = ClipboardOperation.Cut
            )
        }
    }

    /**
     * Copy a single file (via context menu).
     */
    fun copyFile(path: String) {
        _uiState.update {
            it.copy(
                clipboardFiles = listOf(path),
                clipboardOperation = ClipboardOperation.Copy
            )
        }
    }

    /**
     * Cut selected files.
     */
    fun cutSelectedFiles() {
        val selected = _uiState.value.selectedFiles.toList()
        if (selected.isEmpty()) return
        _uiState.update {
            it.copy(
                clipboardFiles = selected,
                clipboardOperation = ClipboardOperation.Cut
            )
        }
        exitMultiSelect()
    }

    /**
     * Copy selected files.
     */
    fun copySelectedFiles() {
        val selected = _uiState.value.selectedFiles.toList()
        if (selected.isEmpty()) return
        _uiState.update {
            it.copy(
                clipboardFiles = selected,
                clipboardOperation = ClipboardOperation.Copy
            )
        }
        exitMultiSelect()
    }

    /**
     * Paste clipboard files to current directory.
     */
    fun pasteFiles(destination: String = _uiState.value.currentPath) {
        val clipboard = _uiState.value.clipboardFiles
        if (clipboard.isEmpty()) return

        viewModelScope.launch {
            when (_uiState.value.clipboardOperation) {
                ClipboardOperation.Cut -> {
                    cutFilesUseCase(clipboard, destination).collect { result ->
                        result.onSuccess {
                            loadFiles(destination)
                            _uiState.update { it.copy(clipboardFiles = emptyList(), clipboardOperation = null) }
                        }
                    }
                }
                ClipboardOperation.Copy -> {
                    copyFilesUseCase(clipboard, destination).collect { result ->
                        result.onSuccess {
                            loadFiles(destination)
                            _uiState.update { it.copy(clipboardFiles = emptyList(), clipboardOperation = null) }
                        }
                    }
                }
                null -> {}
            }
        }
    }

    /**
     * Delete a single file (via context menu).
     */
    fun deleteFile(path: String) {
        viewModelScope.launch {
            deleteFilesUseCase(listOf(path)).collect { result ->
                result.onSuccess {
                    loadFiles(_uiState.value.currentPath)
                }
            }
        }
    }

    /**
     * Delete selected files.
     */
    fun deleteSelectedFiles() {
        val selected = _uiState.value.selectedFiles.toList()
        if (selected.isEmpty()) return

        viewModelScope.launch {
            deleteFilesUseCase(selected).collect { result ->
                result.onSuccess {
                    loadFiles(_uiState.value.currentPath)
                    exitMultiSelect()
                }
            }
        }
    }

    /**
     * Rename a file.
     */
    fun renameFile(oldPath: String, newName: String) {
        viewModelScope.launch {
            renameFileUseCase(oldPath, newName).collect { result ->
                result.onSuccess {
                    loadFiles(_uiState.value.currentPath)
                }
            }
        }
    }

    /**
     * Create a new directory.
     */
    fun createDirectory(name: String) {
        val path = _uiState.value.currentPath
        viewModelScope.launch {
            val fullPath = "$path/$name"
            fileRepository.createDirectory(fullPath).collect { result ->
                result.onSuccess {
                    loadFiles(path)
                }
            }
        }
    }

    /**
     * Open a file.
     */
    fun openFile(fileItem: FileItem) {
        viewModelScope.launch {
            recentFileRepository.addRecentFile(fileItem)
        }
        // Trigger navigation via callback
    }

    /**
     * Get parent path of current directory.
     */
    fun getParentPath(): String {
        val parts = _uiState.value.currentPath.split("/").filter { it.isNotEmpty() }
        return if (parts.size > 1) {
            "/" + parts.dropLast(1).joinToString("/")
        } else {
            "/"
        }
    }

    /**
     * Sort files based on current sort settings.
     */
    private fun sortFiles(files: List<FileItem>): List<FileItem> {
        return when (_uiState.value.sortBy) {
            "size" -> files.sortedWith { a, b ->
                if (_uiState.value.sortOrder == "asc") {
                    compareValues(a.size, b.size)
                } else {
                    compareValues(b.size, a.size)
                }
            }
            "date" -> files.sortedWith { a, b ->
                if (_uiState.value.sortOrder == "asc") {
                    compareValues(a.lastModified, b.lastModified)
                } else {
                    compareValues(b.lastModified, a.lastModified)
                }
            }
            else -> files.sortedWith { a, b ->
                if (_uiState.value.sortOrder == "asc") {
                    a.name.lowercase().compareTo(b.name.lowercase())
                } else {
                    b.name.lowercase().compareTo(a.name.lowercase())
                }
            }
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Compress selected files into a ZIP archive.
     * Falls back to compressing [singleFilePath] when called from context menu (no selection).
     */
    fun compressFiles(archiveName: String, singleFilePath: String? = null) {
        val sources = if (singleFilePath != null) {
            listOf(singleFilePath)
        } else {
            _uiState.value.selectedFiles.toList()
        }
        if (sources.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCompressing = true, compressMessage = "正在压缩...") }

            val parentDir = File(_uiState.value.currentPath)
            val archivePath = "${parentDir.absolutePath}/$archiveName.zip"

            val result = createZipUseCase(sources, archivePath)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isCompressing = false,
                        compressMessage = "压缩成功: $archiveName.zip"
                    )
                }
                if (singleFilePath == null) exitMultiSelect()
                loadFiles(_uiState.value.currentPath)
            } else {
                _uiState.update {
                    it.copy(
                        isCompressing = false,
                        compressMessage = "压缩失败: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    /**
     * Extract a ZIP file to a destination directory.
     */
    fun extractFile(archivePath: String, destinationDir: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExtracting = true, extractMessage = "正在解压...") }

            val result = extractZipUseCase(archivePath, destinationDir)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isExtracting = false,
                        extractMessage = "解压成功"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isExtracting = false,
                        extractMessage = "解压失败: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
}
