package com.filemanager.app

/**
 * Sealed class representing bottom navigation routes.
 * 6 tabs: Home | Files | Latest | Search | More
 */
sealed class BottomNavRoute(val route: String, val title: String) {
    object HOME : BottomNavRoute("home", "首页")
    object BROWSER : BottomNavRoute("browser", "文件")
    object LATEST : BottomNavRoute("latest", "最新")
    object SEARCH : BottomNavRoute("search", "搜索")
    object MORE : BottomNavRoute("more", "更多")
    object COLLECT : BottomNavRoute("collect", "收藏")

    // Sub-screens (not in bottom nav directly)
    object RECENT : BottomNavRoute("recent", "最近")
    object APPS : BottomNavRoute("apps", "应用")
    object CLOUD : BottomNavRoute("cloud", "云存储")
    object SETTINGS : BottomNavRoute("settings", "设置")
}
