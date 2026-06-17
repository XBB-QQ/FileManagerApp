package com.filemanager.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Bottom bar displayed when in multi-select mode.
 * Shows actions: select all, cut, copy, delete, share, compress.
 */
@Composable
fun MultiSelectBar(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onCut: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    hasClipboard: Boolean = false,
    onDelete: () -> Unit,
    onCompress: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        color = Color(0xFFF5F5F5),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "已选 $selectedCount",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onSelectAll) {
                    Icon(Icons.Default.SelectAll, "Select all", tint = Color.Black)
                }
                IconButton(onClick = onCut) {
                    Icon(Icons.Default.ContentCut, "Cut", tint = Color.Black)
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, "Copy", tint = Color.Black)
                }
                if (hasClipboard) {
                    IconButton(onClick = onPaste) {
                        Icon(Icons.Default.ContentPaste, "Paste", tint = Color.Black)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                }
                IconButton(onClick = onCompress) {
                    Icon(Icons.Default.InsertDriveFile, "Compress", tint = Color(0xFFE65100))
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, "Cancel", tint = Color.Black)
                }
            }
        }
    }
}
