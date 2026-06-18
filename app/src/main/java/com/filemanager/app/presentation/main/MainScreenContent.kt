package com.filemanager.app.presentation.main

import android.content.Intent
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType
import com.filemanager.app.presentation.component.*

/**
 * Main file browser screen content.
 * Displays a list of files/folders in the current directory with navigation and operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    onPreviewFile: (String, String?) -> Unit = { _, _ -> },
    onOpenTextFile: (String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onShareFile: (String) -> Unit = {},
    onShowSnackbar: (String) -> Unit = {},
    onOpenSidebar: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Dialogs state
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<FileItem?>(null) }
    var renameName by remember { mutableStateOf("") }
    var showSortDialog by remember { mutableStateOf(false) }
    // Context menu state
    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuFile by remember { mutableStateOf<FileItem?>(null) }
    var contextMenuIsDirectory by remember { mutableStateOf(false) }
    var contextMenuIsArchive by remember { mutableStateOf(false) }
    var contextMenuIsAPK by remember { mutableStateOf(false) }
    var contextMenuHasClipboard by remember { mutableStateOf(false) }
    var contextMenuIsMultiSelect by remember { mutableStateOf(false) }
    var showPropertiesDialog by remember { mutableStateOf(false) }
    var propertiesFile by remember { mutableStateOf<FileItem?>(null) }
    var showCompressDialog by remember { mutableStateOf(false) }
    var compressFileName by remember { mutableStateOf("") }
    var showExtractDialog by remember { mutableStateOf(false) }
    var extractFilePath by remember { mutableStateOf("") }
    var showApkInstallDialog by remember { mutableStateOf(false) }
    var apkFilePath by remember { mutableStateOf("") }

    // Error snackbar
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            onShowSnackbar(error)
            viewModel.clearError()
        }
    }

    // Operation message snackbar (compress/extract) — separated to avoid overlap
    uiState.compressMessage?.let { msg ->
        LaunchedEffect(msg) {
            onShowSnackbar(msg)
            viewModel.clearCompressMessage()
        }
    }
    uiState.extractMessage?.let { msg ->
        LaunchedEffect(msg) {
            onShowSnackbar(msg)
            viewModel.clearExtractMessage()
        }
    }

    val localContext = LocalContext.current
    val canInstallPackages = remember {
        try {
            android.provider.Settings::class.java
                .getMethod("canRequestPackageInstalls", android.content.Context::class.java)
                .invoke(null, localContext) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    // Handle APK install dialog
    if (showApkInstallDialog && apkFilePath.isNotEmpty()) {
        val file = java.io.File(apkFilePath)
        AlertDialog(
            onDismissRequest = {
                showApkInstallDialog = false
                apkFilePath = ""
            },
            title = { Text("安装应用") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("应用名称: ${file.name}", style = MaterialTheme.typography.bodyMedium)
                    Text("文件大小: ${formatApkFileSize(file.length())}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    if (!canInstallPackages) {
                        Text(
                            "需要授予安装未知应用的权限",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = canInstallPackages,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                val uri = FileProvider.getUriForFile(
                                    localContext,
                                    "${localContext.packageName}.fileprovider",
                                    file
                                )
                                setDataAndType(uri, "application/vnd.android.package-archive")
                            }
                            localContext.startActivity(intent)
                        } catch (e: Exception) {
                            onShowSnackbar("安装失败: ${e.message}")
                        }
                        showApkInstallDialog = false
                        apkFilePath = ""
                    }
                ) { Text("安装") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showApkInstallDialog = false
                    apkFilePath = ""
                }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.currentPath.substringAfterLast('/'),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenSidebar) {
                        Icon(Icons.Default.Menu, "文件夹导航")
                    }
                },
                actions = {
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.Default.Sort, "Sort")
                    }
                    IconButton(onClick = { showNewFolderDialog = true }) {
                        Icon(Icons.Default.Add, "New folder")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewFolderDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "New folder")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Path bar
            PathBar(
                path = uiState.currentPath,
                onUpClick = viewModel::goUp
            )

            // Operation toolbar
            OperationToolbar(
                currentPath = uiState.currentPath,
                sortBy = uiState.sortBy,
                sortOrder = uiState.sortOrder,
                showHidden = uiState.showHidden,
                onSortByChanged = viewModel::setSortBy,
                onSortOrderChanged = { viewModel.toggleSortOrder() },
                onShowHiddenChanged = { viewModel.toggleShowHidden() },
                onSearchClick = onNavigateToSearch,
                onSettingsClick = onNavigateToSettings
            )

            // Context menu (dynamic based on file type)
            if (showContextMenu && contextMenuFile != null) {
                val menuItems = getFileContextMenuItems(
                    isMultiSelect = contextMenuIsMultiSelect,
                    hasClipboard = contextMenuHasClipboard,
                    isDirectory = contextMenuIsDirectory,
                    isArchive = contextMenuIsArchive,
                    isAPK = contextMenuIsAPK
                )
                AlertDialog(
                    onDismissRequest = { showContextMenu = false },
                    title = { Text(contextMenuFile?.name ?: "") },
                    text = {
                        Column {
                            menuItems.forEach { item ->
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val file = contextMenuFile!!
                                            when (item.label) {
                                                "全选" -> viewModel.selectAll()
                                                "剪切" -> viewModel.cutFile(file.path)
                                                "复制" -> {
                                                    viewModel.copyFile(file.path)
                                                }
                                                "粘贴" -> viewModel.pasteFiles()
                                                "重命名" -> {
                                                    renameTarget = file
                                                    renameName = file.name
                                                    showRenameDialog = true
                                                }
                                                "删除" -> viewModel.deleteFile(file.path)
                                                "属性" -> {
                                                    propertiesFile = file
                                                    showPropertiesDialog = true
                                                }
                                                "分享" -> onShareFile(file.path)
                                                "添加到收藏" -> {
                                                    // TODO: add to favorites
                                                }
                                                "压缩" -> {
                                                    showCompressDialog = true
                                                    compressFileName = if (file.extension.isNotEmpty()) {
                                                        file.name.removeSuffix("." + file.extension).removeSuffix(".zip")
                                                    } else {
                                                        file.name
                                                    }
                                                }
                                                "解压" -> {
                                                    showExtractDialog = true
                                                    extractFilePath = file.path
                                                }
                                                "编辑" -> {
                                                    onOpenTextFile(file.path)
                                                }
                                                else -> {}
                                            }
                                            showContextMenu = false
                                        }
                                        .padding(vertical = 12.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showContextMenu = false }) {
                            Text("取消")
                        }
                    }
                )
            }

            // File list
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.files.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Create,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("目录为空", color = Color.LightGray)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(uiState.files) { item ->
                            FileItemRow(
                                fileItem = item,
                                isSelected = item.path in uiState.selectedFiles,
                                onClick = {
                                    if (item.type == FileType.DIRECTORY) {
                                        viewModel.navigateToDirectory(item)
                                    } else if (item.type == FileType.APK) {
                                        apkFilePath = item.path
                                        showApkInstallDialog = true
                                    } else {
                                        viewModel.openFile(item)
                                        onPreviewFile(item.path, item.mimeType)
                                    }
                                },
                                onLongClick = {
                                    if (uiState.isMultiSelect) {
                                        viewModel.toggleFileSelection(item.path)
                                    } else {
                                        viewModel.enterMultiSelect(item)
                                        contextMenuFile = item
                                        contextMenuIsDirectory = item.type == FileType.DIRECTORY
                                        contextMenuIsArchive = item.type == FileType.ARCHIVE
                                        contextMenuIsAPK = item.type == FileType.APK
                                        contextMenuHasClipboard = uiState.clipboardFiles.isNotEmpty()
                                        contextMenuIsMultiSelect = true
                                        showContextMenu = true
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Multi-select bar
            if (uiState.isMultiSelect) {
                MultiSelectBar(
                    selectedCount = uiState.selectedFiles.size,
                    onSelectAll = viewModel::selectAll,
                    onCut = viewModel::cutSelectedFiles,
                    onCopy = viewModel::copySelectedFiles,
                    onPaste = { viewModel.pasteFiles() },
                    hasClipboard = uiState.clipboardFiles.isNotEmpty(),
                    onDelete = viewModel::deleteSelectedFiles,
                    onCompress = {
                        // 多选压缩：默认名称为 "压缩包"
                        if (uiState.selectedFiles.isEmpty()) {
                            compressFileName = "压缩包"
                        }
                        showCompressDialog = true
                    },
                    onCancel = viewModel::exitMultiSelect
                )
            }
        }
    }

    // New folder dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("新建文件夹") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = { Text("文件夹名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createDirectory(newFolderName)
                            newFolderName = ""
                            showNewFolderDialog = false
                        }
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Rename dialog
    if (showRenameDialog && renameTarget != null) {
        AlertDialog(
            onDismissRequest = {
                showRenameDialog = false
                renameTarget = null
            },
            title = { Text("重命名") },
            text = {
                OutlinedTextField(
                    value = renameName,
                    onValueChange = { renameName = it },
                    placeholder = { Text("新名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        renameTarget?.let {
                            viewModel.renameFile(it.path, renameName)
                        }
                        showRenameDialog = false
                        renameTarget = null
                        renameName = ""
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRenameDialog = false
                    renameTarget = null
                    renameName = ""
                }) {
                    Text("取消")
                }
            }
        )
    }

    // File properties dialog
    if (showPropertiesDialog && propertiesFile != null) {
        PropertiesDialog(
            file = propertiesFile!!,
            onDismiss = {
                showPropertiesDialog = false
                propertiesFile = null
            }
        )
    }

    // Sort dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("排序方式") },
            text = {
                Column {
                    listOf(
                        "名称" to "name",
                        "大小" to "size",
                        "修改时间" to "date"
                    ).forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label)
                            Text(if (value == uiState.sortBy) "✓" else "")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "排序顺序: ${if (uiState.sortOrder == "asc") "升序 ↑" else "降序 ↓"}",
                        modifier = Modifier.clickable { viewModel.toggleSortOrder() }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    // Compress dialog
    if (showCompressDialog) {
        val infoText = when {
            uiState.selectedFiles.isNotEmpty() -> "已选 ${uiState.selectedFiles.size} 个文件/文件夹"
            compressFileName.isNotEmpty() -> "压缩: $compressFileName.zip"
            else -> null
        }
        OperationDialog(
            title = "压缩文件",
            isWorking = uiState.isCompressing,
            workingMessage = "压缩中...",
            infoText = infoText,
            textFieldValue = compressFileName,
            onTextFieldChange = { compressFileName = it },
            placeholder = "压缩包名称",
            confirmButtonText = "确定",
            onConfirm = {
                if (compressFileName.isNotBlank()) {
                    val singlePath = if (uiState.selectedFiles.isEmpty()) {
                        contextMenuFile?.path
                    } else {
                        null
                    }
                    viewModel.compressFiles(compressFileName, singlePath)
                    compressFileName = ""
                    showCompressDialog = false
                }
            },
            onDismiss = {
                showCompressDialog = false
                compressFileName = ""
            }
        )
    }

    // Extract dialog
    if (showExtractDialog && extractFilePath.isNotEmpty()) {
        OperationDialog(
            title = "解压文件",
            isWorking = uiState.isExtracting,
            workingMessage = "解压中...",
            infoText = "目标目录: ${uiState.currentPath}",
            confirmButtonText = "解压到当前目录",
            onConfirm = {
                viewModel.extractFile(extractFilePath, uiState.currentPath)
                showExtractDialog = false
                extractFilePath = ""
            },
            onDismiss = {
                showExtractDialog = false
                extractFilePath = ""
            }
        )
    }
}

/**
 * Generic dialog for file operations (compress/extract).
 * Provides consistent structure: title, optional info text, optional text field, progress, confirm/dismiss.
 */
@Composable
private fun OperationDialog(
    title: String,
    isWorking: Boolean,
    workingMessage: String,
    infoText: String? = null,
    textFieldValue: String = "",
    onTextFieldChange: ((String) -> Unit)? = null,
    placeholder: String? = null,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var textFieldValueInternal by remember { mutableStateOf(textFieldValue) }
    // Sync external value changes (e.g. from context menu prefill)
    LaunchedEffect(textFieldValue) {
        textFieldValueInternal = textFieldValue
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                infoText?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                if (onTextFieldChange != null) {
                    OutlinedTextField(
                        value = textFieldValueInternal,
                        onValueChange = {
                            textFieldValueInternal = it
                            onTextFieldChange(it)
                        },
                        placeholder = { placeholder?.let { Text(it) } },
                        singleLine = true
                    )
                }
                if (isWorking) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text(workingMessage, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = if (onTextFieldChange != null) {
                    textFieldValueInternal.isNotBlank() && !isWorking
                } else {
                    !isWorking
                },
                onClick = onConfirm
            ) { Text(confirmButtonText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun PropertiesDialog(
    file: FileItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("文件属性") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PropRow("名称", file.name)
                PropRow("类型", fileTypeLabel(file.type, file.extension))
                PropRow("大小", file.humanReadableSize)
                PropRow("修改时间", file.modifiedDate)
                PropRow("路径", file.path)
                PropRow("隐藏", if (file.isHidden) "是" else "否")
                file.childCount?.let { count ->
                    PropRow("子项数", "$count")
                }
                file.mimeType?.let { mime ->
                    PropRow("MIME 类型", mime)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun PropRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun formatApkFileSize(size: Long): String = when {
    size < 0 -> ""
    size < 1024L -> "$size B"
    size < 1024L * 1024 -> String.format("%.1f KB", size / 1024.0)
    size < 1024L * 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
    else -> String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0))
}

private fun fileTypeLabel(type: FileType, extension: String): String {
    return when (type) {
        FileType.DIRECTORY -> "文件夹"
        FileType.IMAGE -> "图片文件 (.$extension)"
        FileType.VIDEO -> "视频文件 (.$extension)"
        FileType.AUDIO -> "音频文件 (.$extension)"
        FileType.DOCUMENT -> "文档文件 (.$extension)"
        FileType.ARCHIVE -> "压缩包 (.$extension)"
        FileType.APK -> "Android 应用 (.$extension)"
        FileType.TEXT -> "文本文档 (.$extension)"
        FileType.XML -> "XML 文件 (.$extension)"
        FileType.CODE -> "源代码文件 (.$extension)"
        FileType.FONT -> "字体文件 (.$extension)"
        FileType.HIDDEN -> "隐藏文件"
        FileType.APACHE_DOC -> "Apache 文档 (.$extension)"
        FileType.UNKNOWN -> "未知文件"
    }
}
