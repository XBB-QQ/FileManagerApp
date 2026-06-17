package com.filemanager.app.presentation.sidebar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.StorageInfo
import com.filemanager.app.domain.repository.FavoriteRepository
import com.filemanager.app.domain.repository.FileRepository
import com.filemanager.app.presentation.sidebar.model.SidebarTreeNode
import com.filemanager.app.presentation.sidebar.util.TreeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the sidebar folder tree navigation.
 * Manages folder tree per storage device, favorites, and storage switching.
 */
@HiltViewModel
class SidebarViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    // Current selected path (synced from MainViewModel in production)
    private val _currentPath = MutableStateFlow("/")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    /** Set the current path from the main browser. */
    fun setCurrentPath(path: String) {
        _currentPath.value = path
    }

    // Storage devices
    private val _storages = MutableStateFlow<List<StorageInfo>>(emptyList())
    val storages: StateFlow<List<StorageInfo>> = _storages.asStateFlow()

    // Folder trees keyed by storage index
    private val _folderTrees = MutableStateFlow<Map<Int, List<SidebarTreeNode>>>(emptyMap())
    val folderTrees: StateFlow<Map<Int, List<SidebarTreeNode>>> = _folderTrees.asStateFlow()

    // Active storage tab
    private val _activeStorageIndex = MutableStateFlow(0)
    val activeStorageIndex: StateFlow<Int> = _activeStorageIndex.asStateFlow()

    // Favorites (directory-only)
    val favoriteDirectories: StateFlow<List<FileItem>> = favoriteRepository
        .getFavorites()
        .map { items -> items.filter { it.type == com.filemanager.app.domain.model.FileType.DIRECTORY } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadStorageInfo()
    }

    /** Load storage info and build initial folder trees. */
    private fun loadStorageInfo() {
        viewModelScope.launch {
            fileRepository.getStorageInfo()
                .collect { result ->
                    result.onSuccess { storages ->
                        _storages.value = storages
                        // Build initial tree for first storage
                        if (storages.isNotEmpty()) {
                            rebuildTree(storages[0].path)
                        }
                    }
                }
        }
    }

    /** Rebuild the folder tree for a given storage path. */
    private fun rebuildTree(storagePath: String) {
        val tree = TreeUtils.buildInitialTree(storagePath)
        val idx = _activeStorageIndex.value
        _folderTrees.update { current ->
            current + mapOf(idx to listOf(tree))
        }
    }

    /** Expand a node and lazily load its children. */
    fun expandNode(storageIndex: Int, node: SidebarTreeNode) {
        viewModelScope.launch {
            val result = TreeUtils.loadChildren(node, fileRepository)
            result.onSuccess { children ->
                val updatedNode = node.copy(
                    children = children,
                    childrenLoaded = true
                )
                _folderTrees.update { current ->
                    val trees = current[storageIndex]?.map { n ->
                        if (n.path == node.path) updatedNode else n
                    } ?: current[storageIndex].orEmpty()
                    current + mapOf(storageIndex to trees)
                }
            }
        }
    }

    /** Toggle expand/collapse of a node. Loads children on first expand. */
    fun toggleExpand(storageIndex: Int, node: SidebarTreeNode) {
        val willExpand = TreeUtils.toggleExpand(node)
        if (willExpand && !node.childrenLoaded) {
            expandNode(storageIndex, node)
        }
    }

    /** Switch the active storage tab. */
    fun switchStorage(index: Int) {
        _activeStorageIndex.value = index
        val storages = _storages.value
        if (index < storages.size) {
            rebuildTree(storages[index].path)
        }
    }

    /** Navigate to a folder. */
    fun selectNode(path: String) {
        _currentPath.value = path
    }
}
