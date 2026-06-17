package com.filemanager.app.presentation.search

import android.text.format.DateUtils
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import com.filemanager.app.presentation.component.FileIcon

/**
 * Search screen for finding files by name.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onFileSelected: (String, String?) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState(initial = emptyList())
    val searchHistory by viewModel.searchHistory.collectAsState(initial = emptyList())
    val isSearching by viewModel.isSearching.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = viewModel::setQuery,
                            placeholder = { Text("搜索文件名...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    } else {
                        Text("搜索")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isSearchActive) {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearQuery() }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.startSearch()
                        }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isSearchActive) {
                // Search results
                when {
                    isSearching -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    searchResults.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("没有找到匹配的文件", color = Color.LightGray)
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(searchResults) { file ->
                                SearchResultItem(file, onClick = {
                                    onFileSelected(file.path, file.mimeType)
                                })
                            }
                        }
                    }
                }
            } else {
                // Search history
                if (searchHistory.isNotEmpty()) {
                    Text(
                        "搜索历史",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(16.dp, 8.dp, 8.dp, 4.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(searchHistory) { history ->
                            ListItem(
                                headlineContent = { Text(history.query) },
                                supportingContent = {
                                    Text(
                                        DateUtils.getRelativeDateTimeString(
                                            LocalContext.current,
                                            history.timestamp,
                                            0,
                                            0,
                                            0
                                        ).toString()
                                    )
                                },
                                leadingContent = {
                                    Icon(Icons.Default.History, null, tint = Color.Gray)
                                },
                                trailingContent = {
                                    IconButton(onClick = { viewModel.removeSearchHistory(history.query) }) {
                                        Icon(Icons.Default.Delete, "删除", tint = Color.Gray)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setQuery(history.query)
                                        viewModel.startSearch()
                                        isSearchActive = true
                                    }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text("输入关键词搜索文件", color = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
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
        FileIcon(file.type, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(file.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${file.humanReadableSize}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
