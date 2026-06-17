package com.filemanager.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.FileType

/**
 * Displays a single file/folder row in the file list.
 * Uses combinedClickable to handle both tap and long-press.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemRow(
    fileItem: FileItem,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Selection checkbox
        if (isSelected) {
            Checkbox(
                checked = true,
                onCheckedChange = null,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(20.dp))
        }

        // File icon
        FileIcon(
            type = fileItem.type,
            modifier = Modifier.size(40.dp)
        )

        // File info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = fileItem.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = fileItem.humanReadableSize,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        // Size/date
        if (fileItem.type != FileType.DIRECTORY) {
            Text(
                text = formatFileSize(fileItem.size),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.width(60.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

/**
 * Format size for display in the row.
 */
private fun formatFileSize(size: Long): String = when {
    size < 0 -> ""
    size < 1024L -> "$size B"
    size < 1024L * 1024 -> String.format("%.1f KB", size / 1024.0)
    size < 1024L * 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
    else -> String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0))
}

/**
 * Checkbox component for multi-select.
 */
@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
    )
}
