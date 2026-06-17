package com.filemanager.app.presentation.cloud

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
 * Placeholder cloud storage screen.
 * TODO: Integrate Google Drive, Dropbox, OneDrive SDKs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudScreen(
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("云存储") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CloudProviderCard(
                name = "Google Drive",
                icon = Icons.Default.Cloud,
                connected = false,
                onConnectClick = {}
            )
            CloudProviderCard(
                name = "Dropbox",
                icon = Icons.Default.Cloud,
                connected = false,
                onConnectClick = {}
            )
            CloudProviderCard(
                name = "OneDrive",
                icon = Icons.Default.Cloud,
                connected = false,
                onConnectClick = {}
            )
            CloudProviderCard(
                name = "百度网盘",
                icon = Icons.Default.Cloud,
                connected = false,
                onConnectClick = {}
            )
        }
    }
}

@Composable
private fun CloudProviderCard(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    connected: Boolean,
    onConnectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onConnectClick,
        colors = CardDefaults.cardColors(
            containerColor = if (connected) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, name, tint = if (connected) Color.Green else Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(name, style = MaterialTheme.typography.bodyMedium)
                    Text(if (connected) "已连接" else "点击连接", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            if (!connected) {
                Button(onClick = onConnectClick) {
                    Text("连接")
                }
            }
        }
    }
}
