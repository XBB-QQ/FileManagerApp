package com.filemanager.app.presentation.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import com.filemanager.app.presentation.component.FileIcon
import com.filemanager.app.presentation.sidebar.model.SidebarTreeNode

/**
 * Sidebar overlay drawer for folder tree navigation, favorites, and storage switching.
 * Rendered as an overlay panel (not a ModalNavigationDrawer) to preserve existing NavHost structure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarDrawer(
    viewModel: SidebarViewModel,
    currentPath: String,
    onNavigateToFolder: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val storages by viewModel.storages.collectAsState()
    val activeIndex by viewModel.activeStorageIndex.collectAsState()
    val folderTrees by viewModel.folderTrees.collectAsState()
    val favoriteDirs by viewModel.favoriteDirectories.collectAsState()

    val drawerWidth = 300.dp

    // Dark overlay backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        // Semi-transparent backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )

        // Slide-in drawer panel
        Column(
            modifier = Modifier
                .width(drawerWidth)
                .align(Alignment.CenterStart)
                .background(MaterialTheme.colorScheme.surface)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // ===== Header =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "文件夹",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "关闭")
                }
            }

            Divider(color = Color.LightGray)

            // ===== Storage Tab Row =====
            if (storages.size > 1) {
                TabRow(
                    selectedTabIndex = activeIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    storages.forEachIndexed { index, storage ->
                        Tab(
                            selected = index == activeIndex,
                            onClick = { viewModel.switchStorage(index) },
                            text = {
                                Text(
                                    text = if (index == 0) "内部存储" else "SD 卡",
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            // ===== Folder Tree Section =====
            val trees = folderTrees[activeIndex] ?: emptyList()
            if (trees.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("加载中...", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(trees, key = { it.path }) { node ->
                        TreeNodeRow(
                            node = node,
                            currentPath = currentPath,
                            viewModel = viewModel,
                            storageIndex = activeIndex,
                            onNavigate = onNavigateToFolder,
                            depth = 0
                        )
                    }
                }
            }

            // ===== Favorites Section =====
            if (favoriteDirs.isNotEmpty()) {
                Divider(color = Color.LightGray)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "收藏文件夹",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                    favoriteDirs.forEach { fileItem ->
                        FavoriteFolderRow(
                            fileItem = fileItem,
                            onNavigate = onNavigateToFolder
                        )
                    }
                }
            }

            // ===== Footer =====
            Divider(color = Color.LightGray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSettings)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Recursively renders a tree node with expand/collapse and indentation.
 */
@Composable
private fun TreeNodeRow(
    node: SidebarTreeNode,
    currentPath: String,
    viewModel: SidebarViewModel,
    storageIndex: Int,
    onNavigate: (String) -> Unit,
    depth: Int
) {
    val isSelected = node.path == currentPath || currentPath.startsWith("${node.path}/")

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.toggleExpand(storageIndex, node)
                    onNavigate(node.path)
                }
                .background(
                    color = if (isSelected) Color(0xFFE3F2FD) else Color.Transparent,
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 12.dp)
                .padding(start = 12.dp + (depth * 16).dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand/collapse chevron
            if (node.canExpand) {
                Icon(
                    imageVector = if (node.isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (node.isExpanded) "折叠" else "展开",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Spacer(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Folder icon
            FileIcon(
                type = FileType.DIRECTORY,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Folder name
            Text(
                text = node.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Render children if expanded
        if (node.isExpanded && node.childrenLoaded) {
            node.children.forEach { child ->
                if (child.isDirectory) {
                    TreeNodeRow(
                        node = child,
                        currentPath = currentPath,
                        viewModel = viewModel,
                        storageIndex = storageIndex,
                        onNavigate = onNavigate,
                        depth = depth + 1
                    )
                }
            }
        }

        // Show loading indicator if expanding but children not yet loaded
        if (node.isExpanded && !node.childrenLoaded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp + ((depth + 1) * 16).dp)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("加载中...", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

/**
 * Row for a favorite folder item.
 */
@Composable
private fun FavoriteFolderRow(
    fileItem: FileItem,
    onNavigate: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate(fileItem.path) }
            .background(Color.Transparent, MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp)
            .padding(start = 12.dp + 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color(0xFFFF7043),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = fileItem.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
