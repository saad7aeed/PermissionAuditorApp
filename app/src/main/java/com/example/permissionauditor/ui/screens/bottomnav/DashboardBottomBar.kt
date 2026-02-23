package com.example.permissionauditor.ui.screens.bottomnav

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun DashboardBottomBar(
    navController: NavController
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(BottomNavItem.Dashboard.route) {
                            saveState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                // ⭐ COLOR CONTROL
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1976D2),   // icon when selected
                    selectedTextColor = Color(0xFF1976D2),   // text when selected
                    indicatorColor = Color(0x331976D2),      // selection pill bg

                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

