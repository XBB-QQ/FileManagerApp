package com.filemanager.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.presentation.apps.AppsScreen
import com.filemanager.app.presentation.cloud.CloudScreen
import com.filemanager.app.presentation.home.HomeScreen
import com.filemanager.app.presentation.latest.LatestScreen
import com.filemanager.app.presentation.main.MainBottomBar
import com.filemanager.app.presentation.main.MainScreenContent
import com.filemanager.app.presentation.main.MainViewModel
import com.filemanager.app.presentation.main.MoreBottomSheet
import com.filemanager.app.presentation.sidebar.SidebarDrawer
import com.filemanager.app.presentation.sidebar.SidebarViewModel
import com.filemanager.app.presentation.preview.AudioPreviewScreen
import com.filemanager.app.presentation.preview.EnhancedImageViewer
import com.filemanager.app.presentation.preview.EnhancedVideoPlayer
import com.filemanager.app.presentation.preview.TextPreviewScreen
import com.filemanager.app.presentation.collect.CollectScreen
import com.filemanager.app.presentation.recent.RecentScreen
import com.filemanager.app.presentation.search.SearchScreen
import com.filemanager.app.presentation.settings.SettingsScreen
import com.filemanager.app.data.local.AppPreferences
import com.filemanager.app.presentation.theme.FileManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Permission groups the app needs to read/write files.
 * Ordered from most specific (Android 13+) to most broad (legacy).
 */
private val MEDIA_PERMISSIONS = run {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    } else {
        arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}

/**
 * Main Activity — entry point of the app.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        Log.d("MainActivity", "Media permissions granted: $permissions")
        _allPermissionsGranted = allGranted
        if (allGranted) {
            checkManageStorage()
        }
    }

    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d("MainActivity", "Manage storage result: ${it.resultCode}")
        _allPermissionsGranted = true
    }

    private var _allPermissionsGranted by mutableStateOf(false)

    @Inject
    lateinit var appPreferences: AppPreferences

    private fun checkManageStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(
                        android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback: try the action version
                    try {
                        val intent = Intent().apply {
                            action = android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                            data = android.net.Uri.parse("package:" + packageName)
                        }
                        startActivity(intent)
                    } catch (_: Exception) {
                        Log.w("MainActivity", "Cannot redirect to manage storage settings", e)
                        // User can manually enable in Settings → Apps → FileManager → All files access
                    }
                }
            } else {
                _allPermissionsGranted = true
            }
        } else {
            _allPermissionsGranted = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var darkMode by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                appPreferences.isDarkMode.collectLatest { enabled ->
                    darkMode = enabled
                }
            }
            FileManagerTheme(darkTheme = darkMode) {
                val needsPermission = MEDIA_PERMISSIONS.any {
                    ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
                }

                if (needsPermission && !_allPermissionsGranted) {
                    PermissionRequestScreen()
                    LaunchedEffect(Unit) {
                        mediaPermissionLauncher.launch(MEDIA_PERMISSIONS)
                    }
                } else {
                    _allPermissionsGranted = true
                    FileManagerAppNavHost()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!_allPermissionsGranted) {
            val needsPermission = MEDIA_PERMISSIONS.any {
                ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
            }
            if (needsPermission) {
                mediaPermissionLauncher.launch(MEDIA_PERMISSIONS)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Re-check MANAGE_EXTERNAL_STORAGE on resume
            checkManageStorage()
        }
    }
}

/** Loading screen while permissions are being requested. */
@Composable
private fun PermissionRequestScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

/** Main navigation host for the entire app. */
@Composable
private fun FileManagerAppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    var showMoreSheet by remember { mutableStateOf(false) }
    var showSidebar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    androidx.navigation.compose.NavHost(
        navController = navController,
        startDestination = BottomNavRoute.HOME.route
    ) {
        // ===== Main tabs =====

        composable(BottomNavRoute.HOME.route) {
            HomeScreen(
                onNavigateToBrowser = {
                    navController.navigate(BottomNavRoute.BROWSER.route) {
                        popUpTo(BottomNavRoute.HOME.route) { inclusive = false }
                    }
                },
                onNavigateToLatest = {
                    navController.navigate(BottomNavRoute.LATEST.route) {
                        popUpTo(BottomNavRoute.HOME.route) { inclusive = false }
                    }
                }
            )
        }

        composable(BottomNavRoute.BROWSER.route) {
            MainScreenContent(
                onPreviewFile = { path, mimeType ->
                    navController.navigate("previewImage/$path/${mimeType ?: ""}")
                },
                onOpenTextFile = { path ->
                    navController.navigate("previewText/$path")
                },
                onNavigateToSearch = {
                    navController.navigate(BottomNavRoute.SEARCH.route) {
                        popUpTo(BottomNavRoute.BROWSER.route) { inclusive = false }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(BottomNavRoute.SETTINGS.route)
                },
                onShareFile = { path ->
                    shareFile(context, path, coroutineScope, snackbarHostState)
                },
                onShowSnackbar = { message ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message ?: "")
                    }
                },
                onOpenSidebar = { showSidebar = true }
            )
        }

        composable(BottomNavRoute.LATEST.route) {
            LatestScreen(
                onFileSelected = { path, mimeType ->
                    navController.navigate("previewImage/$path/${mimeType ?: ""}")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(BottomNavRoute.SEARCH.route) {
            SearchScreen(
                onFileSelected = { path, mimeType ->
                    navController.navigate("previewImage/$path/${mimeType ?: ""}")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ===== More submenu =====

        composable(BottomNavRoute.RECENT.route) {
            RecentScreen(
                onFileSelected = { path, mimeType ->
                    navController.navigate("previewImage/$path/${mimeType ?: ""}")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(BottomNavRoute.COLLECT.route) {
            CollectScreen(
                onFileSelected = { path, mimeType ->
                    navController.navigate("previewImage/$path/${mimeType ?: ""}")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(BottomNavRoute.APPS.route) {
            AppsScreen(onBack = { navController.popBackStack() })
        }

        composable(BottomNavRoute.CLOUD.route) {
            CloudScreen(onBack = { navController.popBackStack() })
        }

        composable(BottomNavRoute.SETTINGS.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        // ===== File preview routes =====

        composable(
            route = "previewImage/{path}/{mimeType}",
            arguments = listOf(
                navArgument("path") { defaultValue = "" },
                navArgument("mimeType") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path") ?: ""
            val mimeType = backStackEntry.arguments?.getString("mimeType") ?: ""
            val category = mimeType.split('/').firstOrNull() ?: "unknown"

            when (category) {
                "image" -> EnhancedImageViewer(
                    filePath = path,
                    mimeType = mimeType,
                    onBack = { navController.popBackStack() },
                    onShare = { shareFile(context, path, coroutineScope, snackbarHostState) }
                )
                "video" -> EnhancedVideoPlayer(
                    videoPath = path,
                    onBack = { navController.popBackStack() }
                )
                "audio" -> AudioPreviewScreen(
                    audioPath = path,
                    onBack = { navController.popBackStack() }
                )
                else -> {
                    // Default: try to open as text, otherwise show a generic viewer
                    TextPreviewScreen(
                        filePath = path,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        composable(
            route = "previewText/{path}",
            arguments = listOf(navArgument("path") { defaultValue = "" })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path") ?: ""
            TextPreviewScreen(
                filePath = path,
                onBack = { navController.popBackStack() }
            )
        }
    }

    // Bottom navigation bar — only shown for main tabs
    val currentRoute = backStackEntry.value?.destination?.route
    val mainRoutes = listOf(
        BottomNavRoute.HOME.route,
        BottomNavRoute.BROWSER.route,
        BottomNavRoute.LATEST.route,
        BottomNavRoute.SEARCH.route
    )

            if (currentRoute != null && currentRoute in mainRoutes) {
        MainBottomBar(
            routes = listOf(
                BottomNavRoute.HOME,
                BottomNavRoute.BROWSER,
                BottomNavRoute.LATEST,
                BottomNavRoute.SEARCH
            ),
            currentRoute = currentRoute,
            onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = false }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onMoreClick = { showMoreSheet = true }
        )
    }

    // More menu bottom sheet
    if (showMoreSheet) {
        MoreBottomSheet(
            onNavigateToRecent = {
                showMoreSheet = false
                navController.navigate(BottomNavRoute.RECENT.route)
            },
            onNavigateToCollect = {
                showMoreSheet = false
                navController.navigate(BottomNavRoute.COLLECT.route)
            },
            onNavigateToApps = {
                showMoreSheet = false
                navController.navigate(BottomNavRoute.APPS.route)
            },
            onNavigateToCloud = {
                showMoreSheet = false
                navController.navigate(BottomNavRoute.CLOUD.route)
            },
            onNavigateToSettings = {
                showMoreSheet = false
                navController.navigate(BottomNavRoute.SETTINGS.route)
            },
            onDismiss = { showMoreSheet = false }
        )
    }

    // Sidebar drawer — only shown on the BROWSER route
    if (showSidebar) {
        val browserEntry = navController.getBackStackEntry(BottomNavRoute.BROWSER.route)
        val sidebarViewModel: SidebarViewModel = hiltViewModel(browserEntry)
        val mainViewModel: MainViewModel = hiltViewModel(browserEntry)
        val uiState by mainViewModel.uiState.collectAsState()
        SidebarDrawer(
            viewModel = sidebarViewModel,
            currentPath = uiState.currentPath,
            onNavigateToFolder = { path ->
                mainViewModel.navigateToPath(path)
                showSidebar = false
            },
            onOpenSettings = {
                showSidebar = false
                navController.navigate(BottomNavRoute.SETTINGS.route)
            },
            onDismiss = { showSidebar = false }
        )
    }
}

/**
 * Share a file via system share sheet.
 */
private fun shareFile(
    context: android.content.Context,
    filePath: String,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    try {
        val file = java.io.File(filePath)
        if (!file.exists()) return

        val uri = android.net.Uri.fromFile(file)
        val extension = file.extension
        val mimeType = when {
            extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> "image/$extension"
            extension in listOf("mp4", "avi", "mkv", "mov") -> "video/*"
            extension in listOf("mp3", "wav", "flac") -> "audio/*"
            extension == "txt" -> "text/plain"
            extension == "pdf" -> "application/pdf"
            else -> "*/*"
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "分享文件")
        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(context, chooser, null)
    } catch (e: Exception) {
        Log.e("FileManager", "Share failed", e)
        scope.launch {
            snackbarHostState.showSnackbar("分享失败: ${e.message}")
        }
    }
}
