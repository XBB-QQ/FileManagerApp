package com.filemanager.app.presentation.sidebar.util

import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import com.filemanager.app.domain.repository.FileRepository
import com.filemanager.app.presentation.sidebar.model.SidebarTreeNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Utility functions for building and manipulating the sidebar folder tree.
 * Provides lazy-loading of directory children and tree traversal helpers.
 */
object TreeUtils {

    /**
     * Build the initial tree root node from a storage path.
     * Creates a single root node (no children yet — lazy loaded on first expand).
     */
    fun buildInitialTree(storagePath: String): SidebarTreeNode {
        val dir = File(storagePath)
        val name = dir.nameIfNotBlank(storagePath)
        return SidebarTreeNode(
            path = storagePath,
            name = name,
            isDirectory = true,
            isExpanded = false,
            childrenLoaded = false,
            children = emptyList()
        )
    }

    /**
     * Lazily load children for a directory node using FileRepository.
     * Runs on IO dispatcher.
     */
    suspend fun loadChildren(
        node: SidebarTreeNode,
        fileRepository: FileRepository
    ): Result<List<SidebarTreeNode>> = withContext(Dispatchers.IO) {
        Result.catch {
            val fileItems = fileRepository.listFiles(node.path, showHidden = false).first()
            fileItems.mapNotNull { item ->
                if (item.type == FileType.DIRECTORY) {
                    SidebarTreeNode(
                        path = item.path,
                        name = item.name,
                        isDirectory = true,
                        isExpanded = false,
                        childrenLoaded = false,
                        children = emptyList()
                    )
                } else {
                    null // Only show directories in tree
                }
            }
        }
    }

    /**
     * Toggle the expanded state of a node, triggering lazy load if needed.
     */
    fun toggleExpand(node: SidebarTreeNode): Boolean {
        val newState = !node.isExpanded
        node.isExpanded = newState
        return newState
    }

    /**
     * Check if any node in the tree matches the current path (for highlighting).
     * Also marks the matched node's ancestors as expanded.
     */
    fun highlightPath(rootNodes: List<SidebarTreeNode>, currentPath: String): List<SidebarTreeNode> {
        return rootNodes.map { node ->
            if (matchOrDescendant(node, currentPath)) {
                markExpanded(node)
            } else {
                node.copy(children = node.children.map { highlightPath(listOf(it)).first() })
            }
        }
    }

    /** Check if the node path or any descendant matches targetPath. */
    private fun matchOrDescendant(node: SidebarTreeNode, targetPath: String): Boolean {
        if (node.path == targetPath) return true
        if (!node.childrenLoaded) return false
        return node.children.any { matchOrDescendant(it, targetPath) }
    }

    /** Mark this node and all ancestors as expanded for path visibility. */
    private fun markExpanded(node: SidebarTreeNode): SidebarTreeNode {
        node.isExpanded = true
        return node.copy(children = node.children.map { markExpanded(it) })
    }
}

/**
 * Get the file name from path, returning [path] if empty.
 */
private fun String.nameIfNotBlank(default: String): String {
    val name = this.substringAfterLast('/')
    return if (name.isNotBlank()) name else default
}
