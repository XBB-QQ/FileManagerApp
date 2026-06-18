package com.filemanager.app.presentation.sidebar.util

import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import com.filemanager.app.domain.repository.FileRepository
import com.filemanager.app.presentation.sidebar.model.SidebarTreeNode
import kotlinx.coroutines.flow.first
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
        val name = dir.name.takeIf { it.isNotBlank() } ?: storagePath
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
     * Runs on IO dispatcher. Returns a Result wrapping the list of directory children.
     */
    suspend fun loadChildren(
        node: SidebarTreeNode,
        fileRepository: FileRepository
    ): Result<List<SidebarTreeNode>> {
        val flowResult: Result<List<FileItem>> = try {
            fileRepository.listFiles(node.path, showHidden = false).first()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val fileItems: List<FileItem> = flowResult.getOrNull() ?: emptyList()
        val children = fileItems.filter { it.type == FileType.DIRECTORY }
        val treeNodeList = children.map { item ->
            SidebarTreeNode(
                path = item.path,
                name = item.name,
                isDirectory = true,
                isExpanded = false,
                childrenLoaded = false,
                children = emptyList()
            )
        }
        return Result.success(treeNodeList)
    }

    /**
     * Toggle the expanded state of a node.
     * Returns true if the node was expanded (caller should trigger lazy load if not loaded).
     */
    fun toggleExpand(node: SidebarTreeNode): Boolean {
        val newState = !node.isExpanded
        node.isExpanded = newState
        return newState
    }

    /**
     * Highlight the node matching currentPath and expand all ancestors.
     * Used to keep the tree in sync when user navigates via the file list.
     */
    fun highlightPath(
        rootNodes: List<SidebarTreeNode>,
        currentPath: String
    ): List<SidebarTreeNode> {
        return rootNodes.map { node ->
            if (matchOrDescendant(node, currentPath)) {
                markExpanded(node)
            } else {
                node.copy(children = highlightChildren(node.children, currentPath))
            }
        }
    }

    /** Recursively highlight matching children. */
    private fun highlightChildren(
        children: List<SidebarTreeNode>,
        currentPath: String
    ): List<SidebarTreeNode> {
        return children.map { child ->
            if (matchOrDescendant(child, currentPath)) {
                markExpanded(child)
            } else {
                child.copy(children = highlightChildren(child.children, currentPath))
            }
        }
    }

    /** Check if the node path or any descendant matches targetPath. */
    private fun matchOrDescendant(node: SidebarTreeNode, targetPath: String): Boolean {
        if (node.path == targetPath) return true
        if (!node.childrenLoaded) return false
        return node.children.any { matchOrDescendant(it, targetPath) }
    }

    /** Mark this node and all descendants as expanded for path visibility. */
    private fun markExpanded(node: SidebarTreeNode): SidebarTreeNode {
        node.isExpanded = true
        return node.copy(children = markExpandedChildren(node.children))
    }

    private fun markExpandedChildren(children: List<SidebarTreeNode>): List<SidebarTreeNode> {
        return children.map { child ->
            child.isExpanded = true
            child.copy(children = markExpandedChildren(child.children))
        }
    }
}
