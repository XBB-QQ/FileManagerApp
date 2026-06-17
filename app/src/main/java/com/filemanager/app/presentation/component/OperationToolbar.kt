package com.filemanager.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

/**
 * Toolbar shown at the top of the file browser.
 * Contains sort, view mode, filter, and search buttons.
 */
@Composable
fun OperationToolbar(
    currentPath: String,
    sortBy: String,
    sortOrder: String,
    showHidden: Boolean,
    onSortByChanged: (String) -> Unit,
    onSortOrderChanged: (String) -> Unit,
    onShowHiddenChanged: (Boolean) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Sort indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "已按 $sortBy ${if (sortOrder == "asc") "↑" else "↓"} 排序",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Sort dropdown
                var showSortMenu by remember { mutableStateOf(false) }
                if (showSortMenu) {
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        listOf(
                            "名称" to "name",
                            "大小" to "size",
                            "修改时间" to "date"
                        ).forEach { (label, value) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onSortByChanged(value)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(onClick = {
                    showSortMenu = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort"
                    )
                }

                // Hidden files toggle
                IconButton(onClick = { onShowHiddenChanged(!showHidden) }) {
                    Icon(
                        imageVector = if (showHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle hidden files",
                        tint = if (showHidden) Color(0xFF1976D2) else Color.Gray
                    )
                }

                // Search
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }

                // Settings
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        }
    }
}
