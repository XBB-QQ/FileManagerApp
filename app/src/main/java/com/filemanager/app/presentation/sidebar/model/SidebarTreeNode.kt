package com.filemanager.app.presentation.sidebar.model

/**
 * Recursive tree node representing a directory in the sidebar folder tree.
 * Supports lazy loading of children and expand/collapse state.
 */
data class SidebarTreeNode(
    val path: String,
    val name: String,
    val isDirectory: Boolean = true,
    var isExpanded: Boolean = false,
    var childrenLoaded: Boolean = false,
    var children: List<SidebarTreeNode> = emptyList()
) {
    /** Whether this node can be expanded (has children or not yet loaded). */
    val canExpand: Boolean
        get() = isDirectory

    /** Whether this node has any expandable descendants in its subtree. */
    val hasExpandableDescendants: Boolean
        get() = if (childrenLoaded) {
            children.any { it.isDirectory && it.canExpand }
        } else {
            true // Assume expandable until we load children
        }
}
