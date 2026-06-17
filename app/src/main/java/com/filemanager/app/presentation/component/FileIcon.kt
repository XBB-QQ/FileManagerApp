package com.filemanager.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.filemanager.app.domain.model.FileType

/**
 * Returns a Color for a given FileType.
 */
@Composable
fun getFileTypeColor(type: FileType): Color = when (type) {
    FileType.DIRECTORY -> Color(0xFF42A5F5)
    FileType.IMAGE -> Color(0xFF66BB6A)
    FileType.VIDEO -> Color(0xFFAB47BC)
    FileType.AUDIO -> Color(0xFFFFA726)
    FileType.DOCUMENT -> Color(0xFF42A5F5)
    FileType.ARCHIVE -> Color(0xFF78909C)
    FileType.APK -> Color(0xFF66BB6A)
    FileType.TEXT -> Color(0xFFEEEEEE)
    FileType.XML -> Color(0xFFFFA726)
    FileType.CODE -> Color(0xFFEC407A)
    FileType.FONT -> Color(0xFF26C6DA)
    FileType.HIDDEN -> Color(0xFF9E9E9E)
    FileType.APACHE_DOC -> Color(0xFF5C6BC0)
    FileType.UNKNOWN -> Color(0xFFBDBDBD)
}

/**
 * Icon for a FileType.
 */
@Composable
fun getFileTypeIcon(type: FileType): androidx.compose.ui.graphics.vector.ImageVector = when (type) {
    FileType.DIRECTORY -> Icons.Default.Folder
    FileType.IMAGE -> Icons.Default.Image
    FileType.VIDEO -> Icons.Default.PlayCircle
    FileType.AUDIO -> Icons.Default.Headphones
    FileType.DOCUMENT -> Icons.Default.Description
    FileType.ARCHIVE -> Icons.Default.InsertDriveFile
    FileType.APK -> Icons.Default.Android
    FileType.TEXT, FileType.XML, FileType.CODE -> Icons.Default.Code
    FileType.FONT -> Icons.Default.FontDownload
    FileType.HIDDEN -> Icons.Default.VisibilityOff
    FileType.APACHE_DOC -> Icons.Default.InsertDriveFile
    FileType.UNKNOWN -> Icons.Default.InsertDriveFile
}

/**
 * Displays the file type icon with a colored background.
 */
@Composable
fun FileIcon(
    type: FileType,
    modifier: Modifier = Modifier.size(40.dp)
) {
    Box(
        modifier = modifier
            .background(
                color = getFileTypeColor(type),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getFileTypeIcon(type),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
