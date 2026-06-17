package com.filemanager.app.presentation.latest

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import com.filemanager.app.presentation.component.FileIcon

/**
 * Latest files screen — shows files sorted by modification time (newest first).
 * Unlike "Recent" (which tracks accessed files), "Latest" tracks modified files.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatestScreen(
    onFileSelected: (String, String?) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    viewModel: LatestViewModel = hiltViewModel()
) {
    val latestFiles by viewModel.latestFiles.collectAsState(initial = emptyList())
    val currentPath by viewModel.currentPath.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("最新文件") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Path selector
                    var showPathPicker by remember { mutableStateOf(false) }
                    if (showPathPicker) {
                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = { showPathPicker = false }
                        ) {
                            listOf(
                                "内部存储" to "/storage/emulated/0",
                                "下载" to "/storage/emulated/0/Download",
                                "DCIM" to "/storage/emulated/0/DCIM",
                                "Documents" to "/storage/emulated/0/Documents",
                                "Pictures" to "/storage/emulated/0/Pictures"
                            ).forEach { (label, path) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.setPath(path)
                                        showPathPicker = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showPathPicker = true }) {
                        Icon(Icons.Default.LocationOn, "Select path")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.refresh() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Current path indicator
            Text(
                text = "路径: $currentPath",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(12.dp, 4.dp, 12.dp, 0.dp)
            )

            if (latestFiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("暂无最新文件", color = Color.LightGray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(latestFiles) { file ->
                        LatestFileItem(file, onClick = {
                            onFileSelected(file.path, file.mimeType)
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun LatestFileItem(
    file: FileItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileIcon(file.type, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(file.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${file.humanReadableSize} · ${file.modifiedDate}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        // Arrow
        Icon(
            Icons.Default.ChevronRight,
            "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
