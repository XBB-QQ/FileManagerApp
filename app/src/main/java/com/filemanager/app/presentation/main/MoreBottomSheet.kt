package com.filemanager.app.presentation.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * More menu modal bottom sheet.
 * Provides access to Recent, Apps, Cloud, Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreBottomSheet(
    onNavigateToRecent: () -> Unit,
    onNavigateToCollect: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToCloud: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val items = listOf(
        MoreMenuItem("最近文件", Icons.Default.History, onNavigateToRecent),
        MoreMenuItem("我的收藏", Icons.Default.Favorite, onNavigateToCollect),
        MoreMenuItem("应用抽屉", Icons.Default.Apps, onNavigateToApps),
        MoreMenuItem("云存储", Icons.Default.Cloud, onNavigateToCloud),
        MoreMenuItem("设置", Icons.Default.Settings, onNavigateToSettings)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF5F5F5)
    ) {
        Text(
            "更多",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items.forEach { item ->
                ListItem(
                    headlineContent = { Text(item.title) },
                    supportingContent = { Text(item.subtitle) },
                    leadingContent = {
                        Icon(item.icon, item.title, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { item.onClick() }
                        .padding(vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class MoreMenuItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
) {
    val subtitle: String
        get() = when (title) {
            "最近文件" -> "查看最近访问的文件"
            "应用抽屉" -> "管理已安装应用"
            "云存储" -> "连接云盘"
            "设置" -> "外观和功能设置"
            else -> ""
        }
}
