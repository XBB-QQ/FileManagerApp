package com.filemanager.app.presentation.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Text editor screen for editing text-based files.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    filePath: String,
    initialContent: String = "",
    onBack: () -> Unit = {}
) {
    var content by remember { mutableStateOf(initialContent) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑器") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Save logic */ }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                }
            )
        }
    ) { padding ->
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            placeholder = { Text("输入内容...") },
            maxLines = Int.MAX_VALUE
        )
    }
}
