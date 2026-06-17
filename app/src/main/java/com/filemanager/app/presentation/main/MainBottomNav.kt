package com.filemanager.app.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filemanager.app.BottomNavRoute

/**
 * Bottom navigation bar with 4 main tabs + More menu.
 * Shows 首页 | 文件 | 最新 | 搜索 + 更多(底部弹窗)
 */
@Composable
fun MainBottomBar(
    routes: List<BottomNavRoute>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onMoreClick: () -> Unit
) {
    val navBarPadding = WindowInsets.systemBars.asPaddingValues()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(navBarPadding)
            .background(Color(0xFFF5F5F5)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            contentColor = Color.Black,
            modifier = Modifier.fillMaxWidth()
        ) {
            routes.forEach { route ->
                NavigationBarItem(
                    selected = currentRoute == route.route,
                    onClick = { onNavigate(route.route) },
                    icon = {
                        Icon(
                            when (route.route) {
                                "home" -> Icons.Default.Home
                                "browser" -> Icons.Default.Folder
                                "latest" -> Icons.Default.Schedule
                                "search" -> Icons.Default.Search
                                else -> Icons.Default.MoreHoriz
                            },
                            route.title,
                            tint = if (currentRoute == route.route) Color(0xFF1976D2) else Color.Gray
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1976D2),
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
            // More button
            NavigationBarItem(
                selected = false,
                onClick = onMoreClick,
                icon = {
                    Icon(
                        Icons.Default.MoreHoriz,
                        "更多",
                        tint = Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
