package com.example.permissionauditor.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.permissionauditor.ui.screens.bottomnav.BottomNavItem
import com.example.permissionauditor.ui.screens.bottomnav.DashboardBottomBar
import com.example.permissionauditor.ui.screens.bottomnav.bottom_screens.DashboardScreen
import com.example.permissionauditor.ui.screens.bottomnav.bottom_screens.SettingsScreen

@Composable
fun MainScreen(
    onAppClick: (String) -> Unit,
    onReloadClick: () -> Unit
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            DashboardBottomBar(bottomNavController)
        }
    ) { padding ->

        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen(
                    onAppClick = onAppClick,
                    onReloadClick = onReloadClick
                )
            }

            composable(BottomNavItem.Settings.route) {
                SettingsScreen()
            }

        }
    }
}
