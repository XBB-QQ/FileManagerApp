package com.filemanager.app.presentation.preview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * Text file preview/edit screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextPreviewScreen(
    filePath: String,
    onBack: () -> Unit = {}
) {
    var textContent by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }

    // Load file content
    val context = LocalContext.current
    LaunchedEffect(filePath) {
        val uri = android.net.Uri.parse(filePath)
        val isContentUri = uri.scheme == "content" || uri.scheme == null
        try {
            textContent = if (isContentUri) {
                context.contentResolver
                    .openInputStream(uri)?.bufferedReader()?.readText() ?: "无法读取文件"
            } else {
                File(filePath).readText()
            }
        } catch (e: Exception) {
            textContent = "无法读取文件: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "编辑" else "文本预览") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isEditMode = !isEditMode }) {
                        Icon(
                            if (isEditMode) Icons.Filled.Save else Icons.Filled.Edit,
                            if (isEditMode) "保存" else "编辑"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isEditMode) {
            OutlinedTextField(
                value = textContent,
                onValueChange = { textContent = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                maxLines = Int.MAX_VALUE
            )
        } else {
            Text(
                text = textContent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                color = Color.Black
            )
        }
    }
}
