package com.filemanager.app.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.filemanager.app.domain.model.StorageInfo
import com.filemanager.app.presentation.component.FileIcon
import com.filemanager.app.presentation.theme.PrimaryBlue

/**
 * Home screen showing storage overview and quick actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBrowser: () -> Unit = {},
    onNavigateToLatest: () -> Unit = {},
    onNavigateToRecent: () -> Unit = {},
    onNavigateToCloud: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val storages by viewModel.storages.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("首页") },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage info cards
            if (storages.isNotEmpty()) {
                storages.forEach { storage ->
                    StorageCard(storage)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Text("正在加载存储信息...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Quick actions
            Text("快捷操作", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.Folder,
                    label = "文件浏览",
                    onClick = onNavigateToBrowser
                )
                QuickActionCard(
                    icon = Icons.Default.Schedule,
                    label = "最新文件",
                    onClick = onNavigateToLatest
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.History,
                    label = "最近文件",
                    onClick = onNavigateToRecent
                )
                QuickActionCard(
                    icon = Icons.Default.Cloud,
                    label = "云存储",
                    onClick = onNavigateToCloud
                )
            }
        }
    }
}

@Composable
private fun StorageCard(storage: StorageInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(storage.name, style = MaterialTheme.typography.titleMedium)
                if (storage.isRemovable) {
                    Icon(Icons.Default.SdCard, "SD Card", tint = PrimaryBlue)
                }
            }
            LinearProgressIndicator(
                progress = storage.usagePercent / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("已用 ${storage.humanUsed}", style = MaterialTheme.typography.bodySmall)
                Text("${storage.humanFree} 可用", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, label, modifier = Modifier.size(32.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
