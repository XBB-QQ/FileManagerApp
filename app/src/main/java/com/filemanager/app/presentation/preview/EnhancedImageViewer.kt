package com.filemanager.app.presentation.preview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import java.io.File

/**
 * Enhanced image viewer with zoom, rotation, fullscreen, and gallery mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedImageViewer(
    filePath: String,
    mimeType: String? = null,
    onBack: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    var isFullscreen by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }

    if (isFullscreen) {
        BackHandler { isFullscreen = false }
        FullscreenImageViewer(
            filePath = filePath,
            mimeType = mimeType,
            onExit = { isFullscreen = false },
            onShowInfo = { showInfoSheet = true },
            onShare = onShare
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("图片预览") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isFullscreen = true }) {
                            Icon(Icons.Default.Fullscreen, "Fullscreen")
                        }
                        IconButton(onClick = { showInfoSheet = true }) {
                            Icon(Icons.Default.Info, "Info")
                        }
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, "Share")
                        }
                    }
                )
            }
        ) { padding ->
            BasicImageViewer(
                filePath = filePath,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }

    if (showInfoSheet) {
        FileInfoBottomSheet(
            filePath = filePath,
            onDismiss = { showInfoSheet = false }
        )
    }
}

/**
 * Basic image viewer with zoom, pan, and double-tap zoom.
 */
@Composable
private fun BasicImageViewer(
    filePath: String,
    modifier: Modifier = Modifier
) {
    var zoomState by remember { mutableStateOf(ZoomState()) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(filePath)
                .build(),
            contentDescription = "Image preview",
            modifier = modifier
                .pointerInput(Unit) {
                    detectTransformGestures(
                        onGesture = { center, pan, zoom, _ ->
                            val newScale = (zoomState.scale * zoom).coerceIn(1f, 10f)
                            zoomState = zoomState.copy(
                                scale = newScale,
                                offsetX = (zoomState.offsetX + pan.x).coerceIn(-1000f, 1000f),
                                offsetY = (zoomState.offsetY + pan.y).coerceIn(-1000f, 1000f)
                            )
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            zoomState = if (zoomState.scale > 1f) {
                                ZoomState() // double-tap to reset
                            } else {
                                ZoomState(scale = 2.5f) // double-tap to zoom in
                            }
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = zoomState.scale
                    scaleY = zoomState.scale
                    translationX = zoomState.offsetX
                    translationY = zoomState.offsetY
                }
                .clickable {
                    zoomState = ZoomState()
                },
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
}

/**
 * Fullscreen immersive image viewer with zoom and pan.
 */
@Composable
private fun FullscreenImageViewer(
    filePath: String,
    mimeType: String? = null,
    onExit: () -> Unit,
    onShowInfo: () -> Unit,
    onShare: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    var zoomState by remember { mutableStateOf(ZoomState()) }
    var showActions by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls },
                    onDoubleTap = {
                        zoomState = if (zoomState.scale > 1f) {
                            ZoomState() // double-tap to reset
                        } else {
                            ZoomState(scale = 2.5f) // double-tap to zoom in
                        }
                    },
                    onLongPress = { showActions = true }
                )
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(filePath)
                .build(),
            contentDescription = "Full image",
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures(
                        onGesture = { center, pan, zoom, _ ->
                            val newScale = zoomState.scale * zoom
                            val clampedScale = newScale.coerceIn(1f, 10f)
                            zoomState = zoomState.copy(
                                scale = clampedScale,
                                offsetX = (zoomState.offsetX + pan.x).coerceIn(-1000f, 1000f),
                                offsetY = (zoomState.offsetY + pan.y).coerceIn(-1000f, 1000f)
                            )
                        }
                    )
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

        if (showControls) {
            Column {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = onExit) {
                            Icon(Icons.Default.Close, "Exit", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = onShowInfo) {
                            Icon(Icons.Default.Info, "Info", tint = Color.White)
                        }
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, "Share", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }

        if (showControls && zoomState.scale > 1f) {
            Text(
                text = "${(zoomState.scale * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        if (showActions) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF333333))
                ) {
                    Column {
                        listOf(
                            "保存相册" to { /* save to gallery */ },
                            "分享" to { onShare() },
                            "详细信息" to { onShowInfo() }
                        ).forEach { (label, action) ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showActions = false; action() }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileInfoBottomSheet(
    filePath: String,
    onDismiss: () -> Unit
) {
    val file = File(filePath)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("文件信息") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FileInfoRow("文件名", file.name)
                FileInfoRow("路径", file.absolutePath)
                FileInfoRow("大小", formatSize(file.length()))
                FileInfoRow("修改时间", file.lastModified().toDateString())
                if (filePath.contains('.')) {
                    FileInfoRow("扩展名", file.extension)
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

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
    else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
}

private fun shareFile(filePath: String) {
    // Placeholder
}
