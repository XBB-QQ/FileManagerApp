package com.filemanager.app

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.filemanager.app.presentation.apps.AppsScreen
import com.filemanager.app.presentation.cloud.CloudScreen
import com.filemanager.app.presentation.collect.CollectScreen
import com.filemanager.app.presentation.home.HomeScreen
import com.filemanager.app.presentation.latest.LatestScreen
import com.filemanager.app.presentation.main.MainBottomBar
import com.filemanager.app.presentation.main.MainScreenContent
import com.filemanager.app.presentation.main.MoreBottomSheet
import com.filemanager.app.presentation.preview.EnhancedImageViewer
import com.filemanager.app.presentation.preview.EnhancedVideoPlayer
import com.filemanager.app.presentation.preview.TextPreviewScreen
import com.filemanager.app.presentation.recent.RecentScreen
import com.filemanager.app.presentation.search.SearchScreen
import com.filemanager.app.presentation.settings.SettingsScreen
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Top-level navigation graph for the app.
 * Manages bottom navigation tabs (4 main + More menu).
 *
 * Bottom Nav: 首页 | 文件 | 最新 | 搜索 | 更多
 * More menu → 最近文件 | 应用 | 云存储 | 设置
 */
@Composable
fun FileManagerNavHost(
    modifier: Modifier = Modifier
) {
    Log.d("FileManagerNavHost", "NavHost started")
    val navController = rememberNavController()
    var showMoreSheet by remember { mutableStateOf(false) }

    androidx.navigation.compose.NavHost(
        navController = navController,
        startDestination = BottomNavRoute.HOME.route,
        modifier = modifier
    ) {
        // ===== Main tabs =====

        composable(BottomNavRoute.HOME.route) {
            Log.d("NavHost", "Navigation to HOME")
            HomeScreen(
                onNavigateToBrowser = {
                    Log.d("NavHost", "Navigate to BROWSER")
                    navController.navigate(BottomNavRoute.BROWSER.route) { launchSingleTop = true }
                },
                onNavigateToLatest = {
                    Log.d("NavHost", "Navigate to LATEST")
                    navController.navigate(BottomNavRoute.LATEST.route) { launchSingleTop = true }
                }
            )
        }

        composable(BottomNavRoute.BROWSER.route) {
            Log.d("NavHost", "Navigation to BROWSER")
            MainScreenContent(
                onPreviewFile = { path, mimeType ->
                    Log.d("NavHost", "Preview file: $path, MIME: $mimeType")
                    navController.navigate("preview/$path/$mimeType")
                },
                onNavigateToSearch = {
                    Log.d("NavHost", "Navigate to SEARCH")
                    navController.navigate(BottomNavRoute.SEARCH.route) {
                        popUpTo(BottomNavRoute.BROWSER.route) { inclusive = false }
                    }
                },
                onNavigateToSettings = {
                    Log.d("NavHost", "Navigate to SETTINGS")
                    navController.navigate(BottomNavRoute.SETTINGS.route)
                }
            )
        }

        composable(BottomNavRoute.LATEST.route) {
            Log.d("NavHost", "Navigation to LATEST")
            LatestScreen(
                onFileSelected = { path, mimeType ->
                    Log.d("NavHost", "File selected in LATEST: $path")
                    navController.navigate("preview/$path/$mimeType")
                },
                onBack = { 
                    Log.d("NavHost", "Back from LATEST")
                    navController.popBackStack() 
                }
            )
        }

        composable(BottomNavRoute.SEARCH.route) {
            Log.d("NavHost", "Navigation to SEARCH")
            SearchScreen(
                onFileSelected = { path, mimeType ->
                    Log.d("NavHost", "File selected in SEARCH: $path")
                    navController.navigate("preview/$path/$mimeType")
                },
                onBack = { 
                    Log.d("NavHost", "Back from SEARCH")
                    navController.popBackStack() 
                }
            )
        }

        // ===== More submenu =====

        composable(BottomNavRoute.RECENT.route) {
            Log.d("NavHost", "Navigation to RECENT")
            RecentScreen(
                onFileSelected = { path, mimeType ->
                    Log.d("NavHost", "File selected in RECENT: $path")
                    navController.navigate("preview/$path/$mimeType")
                },
                onBack = { 
                    Log.d("NavHost", "Back from RECENT")
                    navController.popBackStack() 
                }
            )
        }

        composable(BottomNavRoute.COLLECT.route) {
            Log.d("NavHost", "Navigation to COLLECT")
            CollectScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(BottomNavRoute.APPS.route) {
            Log.d("NavHost", "Navigation to APPS")
            AppsScreen(onBack = { navController.popBackStack() })
        }

        composable(BottomNavRoute.CLOUD.route) {
            Log.d("NavHost", "Navigation to CLOUD")
            CloudScreen(onBack = { navController.popBackStack() })
        }

        composable(BottomNavRoute.SETTINGS.route) {
            Log.d("NavHost", "Navigation to SETTINGS")
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        // ===== File preview =====

        composable(
            route = "preview/{path}/{mimeType}",
            arguments = listOf(
                navArgument("path") { defaultValue = "" },
                navArgument("mimeType") { defaultValue = "" }
            )
        ) { backStackEntry ->
            Log.d("NavHost", "Navigation to PREVIEW")
            val path = backStackEntry.arguments?.getString("path") ?: ""
            val mimeType = backStackEntry.arguments?.getString("mimeType") ?: ""
            Log.d("NavHost", "Preview path: $path, MIME: $mimeType")
            EnhancedPreviewScreen(
                filePath = path,
                mimeType = mimeType,
                onBack = { 
                    Log.d("NavHost", "Back from PREVIEW")
                    navController.popBackStack() 
                }
            )
        }
    }

    // Bottom navigation bar — shown for main tabs
    val navRoutes = listOf(
        BottomNavRoute.HOME,
        BottomNavRoute.BROWSER,
        BottomNavRoute.LATEST,
        BottomNavRoute.SEARCH
    )
    val currentRoute = navController.currentDestination?.route
    Log.d("FileManagerNavHost", "Current route: $currentRoute, Showing nav: ${currentRoute != null && navRoutes.any { it.route == currentRoute }}")

    if (currentRoute != null && navRoutes.any { it.route == currentRoute }) {
        MainBottomBar(
            routes = navRoutes,
            currentRoute = currentRoute,
            onNavigate = { route ->
                Log.d("FileManagerNavHost", "Bottom nav clicked: $route")
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = false }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onMoreClick = { 
                Log.d("FileManagerNavHost", "More menu clicked")
                showMoreSheet = true 
            }
        )
    }

    // More menu bottom sheet
    if (showMoreSheet) {
        Log.d("FileManagerNavHost", "Showing MoreBottomSheet")
        MoreBottomSheet(
            onNavigateToRecent = {
                showMoreSheet = false
                Log.d("FileManagerNavHost", "Navigate to RECENT from More")
                navController.navigate(BottomNavRoute.RECENT.route)
            },
            onNavigateToCollect = {
                showMoreSheet = false
                navController.navigate(BottomNavRoute.COLLECT.route)
            },
            onNavigateToApps = {
                showMoreSheet = false
                Log.d("FileManagerNavHost", "Navigate to APPS from More")
                navController.navigate(BottomNavRoute.APPS.route)
            },
            onNavigateToCloud = {
                showMoreSheet = false
                Log.d("FileManagerNavHost", "Navigate to CLOUD from More")
                navController.navigate(BottomNavRoute.CLOUD.route)
            },
            onNavigateToSettings = {
                showMoreSheet = false
                Log.d("FileManagerNavHost", "Navigate to SETTINGS from More")
                navController.navigate(BottomNavRoute.SETTINGS.route)
            },
            onDismiss = { 
                showMoreSheet = false
                Log.d("FileManagerNavHost", "MoreBottomSheet dismissed")
            }
        )
    }
    
    Log.d("FileManagerNavHost", "NavHost completed")
}

/**
 * Enhanced preview router that picks the right viewer based on MIME type.
 */
@Composable
private fun EnhancedPreviewScreen(
    filePath: String,
    mimeType: String?,
    onBack: () -> Unit
) {
    val category = mimeType?.split('/')?.get(0) ?: "unknown"

    when (category) {
        "image" -> EnhancedImageViewer(
            filePath = filePath,
            mimeType = mimeType,
            onBack = onBack
        )
        "video" -> EnhancedVideoPlayer(
            videoPath = filePath,
            onBack = onBack
        )
        "audio" -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("音频播放开发中...", color = Color.Gray)
            }
        }
        "text" -> TextPreviewScreen(
            filePath = filePath,
            onBack = onBack
        )
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "暂不支持预览此类型: $mimeType",
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Error screen shown when navigation crashes.
 * Displays error message and exit button.
 */
@Composable
private fun ErrorScreen(
    error: Throwable,
    onRestart: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "应用发生错误",
            fontSize = 20.sp,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = error.message ?: "Unknown error",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Android ${android.os.Build.VERSION.RELEASE} | SDK ${android.os.Build.VERSION.SDK_INT}",
            fontSize = 12.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = onRestart,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text("退出应用")
        }
    }
}
