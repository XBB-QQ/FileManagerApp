package com.filemanager.app.presentation.collect

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
 * Favorites screen — shows files/folders the user has marked as favorite.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectScreen(
    onFileSelected: (String, String?) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    viewModel: CollectViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的收藏") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (favorites.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearAll) {
                            Icon(Icons.Default.Delete, "Clear all")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FavoriteBorder, null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("还没有收藏", color = Color.LightGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(favorites) { file ->
                    FavoriteItem(
                        file = file,
                        onClick = { onFileSelected(file.path, file.mimeType) },
                        onRemove = { viewModel.removeFavorite(file.path) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteItem(
    file: FileItem,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileIcon(file.type, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(file.name, style = MaterialTheme.typography.bodyMedium)
            Text(file.modifiedDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        // Favorite icon (filled)
        Icon(
            Icons.Default.Favorite, "Favorite",
            tint = Color.Red,
            modifier = Modifier.size(20.dp)
        )
        // Remove button
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, "Remove", tint = Color.Gray)
        }
    }
}
