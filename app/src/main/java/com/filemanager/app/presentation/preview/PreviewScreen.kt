package com.filemanager.app.presentation.preview

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
 * Unified file preview screen. Routes to the appropriate preview based on MIME type.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    filePath: String,
    mimeType: String?,
    onBack: () -> Unit = {}
) {
    val category = mimeType?.split('/')?.get(0) ?: "unknown"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文件预览") },
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
        ) {
            when (category) {
                "image" -> ImagePreviewScreen(filePath, onBack = onBack)
                "video" -> VideoPreviewScreen(filePath, onBack)
                "audio" -> AudioPreviewScreen(filePath, onBack)
                "text" -> TextPreviewScreen(filePath, onBack)
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.InsertDriveFile,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("暂不支持预览此类型文件", color = Color.LightGray)
                            Text(mimeType ?: "未知类型", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
