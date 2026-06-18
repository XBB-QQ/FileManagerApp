package com.filemanager.app.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

/**
 * Image preview content with proper zoom/pan support.
 * NOTE: This composable is called inside PreviewScreen's Scaffold body,
 * so it does NOT include its own Scaffold/TopAppBar.
 *
 * Supports:
 * - Pinch-to-zoom (scale 1x ~ 10x)
 * - Pan when zoomed
 * - Tap to reset zoom
 * - Zoom percentage indicator
 */
@Composable
fun ImagePreviewScreen(
    filePath: String,
    mimeType: String? = null,
    onBack: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    var zoomState by remember { mutableStateOf(ZoomState()) }
    var showInfoSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(filePath)
                .build(),
            contentDescription = "Image preview",
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomState = zoomState.copy(
                            scale = (zoomState.scale * zoom).coerceIn(1f, 10f),
                            offsetX = (zoomState.offsetX + pan.x).coerceIn(-500f, 500f),
                            offsetY = (zoomState.offsetY + pan.y).coerceIn(-500f, 500f)
                        )
                    }
                }
                .graphicsLayer {
                    scaleX = zoomState.scale
                    scaleY = zoomState.scale
                    translationX = zoomState.offsetX
                    translationY = zoomState.offsetY
                }
                .clickable { zoomState = ZoomState() },
            contentScale = ContentScale.Fit
        )

        if (zoomState.scale > 1f) {
            Text(
                text = "${(zoomState.scale * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }

    // File info bottom sheet
    if (showInfoSheet) {
        FileInfoBottomSheet(
            filePath = filePath,
            onDismiss = { showInfoSheet = false }
        )
    }
}

@Composable
private fun FileInfoBottomSheet(
    filePath: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val file = runCatching { File(filePath) }.getOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("文件信息") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FileInfoRow("文件名", File(filePath).name)
                if (file != null && file.exists()) {
                    FileInfoRow("路径", file.absolutePath)
                    FileInfoRow("大小", formatSize(file.length()))
                    FileInfoRow("修改时间", file.lastModified().toDateString())
                } else {
                    // SAF/content URI — try to get info from ContentResolver
                    val uri = android.net.Uri.parse(filePath)
                    val resolver = context.contentResolver
                    var mimeType by remember { mutableStateOf(resolver.getType(uri)) }
                    var fileSize by remember { mutableStateOf<Long?>(null) }
                    var lastModified by remember { mutableStateOf<Long?>(null) }

                    // Try to get file size and last modified from URI
                    try {
                        resolver.openInputStream(uri)?.use { stream ->
                            fileSize = stream.available().toLong()
                        }
                    } catch (_: Exception) {}

                    try {
                        resolver.query(
                            uri,
                            arrayOf(
                                android.provider.OpenableColumns.DISPLAY_NAME,
                                android.provider.OpenableColumns.SIZE
                            ),
                            null, null
                        )?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                if (nameIdx >= 0) mimeType = cursor.getString(nameIdx)
                                val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                                if (sizeIdx >= 0) fileSize = cursor.getLong(sizeIdx)
                            }
                        }
                    } catch (_: Exception) {}

                    FileInfoRow("路径", filePath)
                    FileInfoRow("大小", fileSize?.let { formatSize(it) } ?: "未知")
                    FileInfoRow("修改时间", lastModified?.toDateString() ?: "未知")
                }
                if (filePath.contains('.')) {
                    FileInfoRow("扩展名", filePath.substringAfterLast('.', ""))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun FileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun Long.toDateString(): String {
    val date = java.util.Date(this)
    return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)
}

data class ZoomState(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
    else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
}
