package com.filemanager.app.presentation.apps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import android.content.pm.PackageManager

/**
 * App drawer showing installed apps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    onBack: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val pm = remember(ctx) { ctx.packageManager }
    val apps = remember(pm) { getInstalledApps(pm) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("应用抽屉") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (apps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("没有安装应用", color = Color.LightGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(apps) { app ->
                    AppItem(app)
                }
            }
        }
    }
}

data class InstalledApp(
    val name: String,
    val packageName: String
)

private fun getInstalledApps(pm: PackageManager): List<InstalledApp> {
    val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
        addCategory(android.content.Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

    return resolveInfos.mapNotNull { info ->
        val label = info.loadLabel(pm)?.toString() ?: return@mapNotNull null
        InstalledApp(
            name = label,
            packageName = info.activityInfo.packageName
        )
    }.sortedBy { it.name }
}

/**
 * Generate a deterministic color from app name for visual distinction.
 */
@Composable
private fun appNameColor(name: String): Color {
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
        Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4DB6AC),
        Color(0xFFFF8A65), Color(0xFF9575CD), Color(0xFF7986CB),
        Color(0xFFAED581)
    )
    return colors[computeHash(name) % colors.size]
}

private fun computeHash(s: String): Int {
    var hash = 1
    for (c in s) hash = hash * 31 + c.toInt()
    return kotlin.math.abs(hash)
}

@Composable
private fun AppItem(app: InstalledApp) {
    val color = appNameColor(app.name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon placeholder (colored circle with first letter)
        Box(
            modifier = Modifier.size(40.dp).background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val letter = app.name.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
            Text(
                text = letter,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))
        Text(app.name, style = MaterialTheme.typography.bodyMedium)
    }
}
