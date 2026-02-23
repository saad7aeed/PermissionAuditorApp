package com.example.permissionauditor.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Main : Screen("main")
    data object Scan : Screen("scan")
    object NewApps : Screen("new_apps")  // ✅ Add new screen
    data object AppDetail : Screen("app_detail/{pkg}") {
        fun create(pkg: String) = "app_detail/$pkg"
    }
}