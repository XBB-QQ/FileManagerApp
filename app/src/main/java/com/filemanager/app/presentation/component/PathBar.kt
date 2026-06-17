package com.filemanager.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Breadcrumb navigation showing the current path segments.
 * Clicking a segment navigates to that directory level.
 */
@Composable
fun PathBar(
    path: String,
    onSegmentClick: (String) -> Unit = {},  // full path to navigate to
    onUpClick: () -> Unit = {}
) {
    val segments = path.split("/").filter { it.isNotEmpty() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Up button
        IconButton(
            onClick = onUpClick,
            enabled = segments.isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = "Up",
                modifier = Modifier.size(20.dp),
                tint = if (segments.isNotEmpty()) Color(0xFF1976D2) else Color.Gray
            )
        }

        // Breadcrumb segments
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            segments.forEachIndexed { index, segment ->
                val segmentPath = "/" + segments.take(index + 1).joinToString("/")
                Text(
                    text = if (index == segments.size - 1) segment else "$segment/",
                    fontSize = 12.sp,
                    color = if (index == segments.size - 1) Color.Black else Color(0xFF1976D2),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable {
                        onSegmentClick(segmentPath)
                    }
                )
                if (index < segments.size - 1) {
                    Text(text = "›", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}
