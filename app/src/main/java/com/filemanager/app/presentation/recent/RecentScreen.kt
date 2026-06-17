package com.filemanager.app.presentation.recent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import com.filemanager.app.presentation.component.FileIcon

/**
 * Recent files screen showing recently accessed files.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentScreen(
    onFileSelected: (String, String?) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    viewModel: RecentViewModel = hiltViewModel()
) {
    val recentFiles by viewModel.recentFiles.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("最近文件") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (recentFiles.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearRecent) {
                            Icon(Icons.Default.Delete, "Clear all")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (recentFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("还没有最近文件", color = Color.LightGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(recentFiles) { file ->
                    RecentFileItem(file, onClick = {
                        onFileSelected(file.path, file.mimeType)
                    })
                }
            }
        }
    }
}

@Composable
private fun RecentFileItem(
    file: FileItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        FileIcon(file.type, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(file.name, style = MaterialTheme.typography.bodyMedium)
            Text(file.modifiedDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Text(file.humanReadableSize, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
