package com.example.permissionauditor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.permissionauditor.ui.screens.AppDetailScreen
import com.example.permissionauditor.ui.screens.MainScreen
import com.example.permissionauditor.ui.screens.WelcomeScreen
import com.example.permissionauditor.ui.screens.bottomnav.NewAppsScreen
import com.example.permissionauditor.ui.screens.bottomnav.ScanningScreen
import com.example.permissionauditor.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavGraph(
    navigateToRoute: String? = null
) {
    val navController = rememberNavController()
    val viewModel: DashboardViewModel = viewModel()

    // ✅ Listen for navigation events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.navigateToNewApps.collectLatest { shouldNavigate ->
            if (shouldNavigate) {
                navController.navigate(Screen.NewApps.route) {
                    launchSingleTop = true
                }
            }
        }
    }

    // ✅ Handle navigation from notification
    LaunchedEffect(navigateToRoute) {
        if (navigateToRoute == "new_apps") {
            navController.navigate(Screen.NewApps.route) {
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Scan.route
    ) {

        // ---------------- WELCOME ----------------
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onStartScan = {
                    navController.navigate(Screen.Scan.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        // ---------------- SCANNING ----------------
        composable(Screen.Scan.route) {
            ScanningScreen(
                onScanComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Scan.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ---------------- MAIN ----------------
        composable(Screen.Main.route) {
            MainScreen(
                onAppClick = { pkg ->
                    navController.navigate(Screen.AppDetail.create(pkg))
                },
                onReloadClick = {
                    navController.navigate(Screen.Scan.route) {
                        popUpTo(Screen.Scan.route) { inclusive = true }
                    }
                }
            )
        }

        // ---------------- NEW APPS SCREEN ----------------
        composable(Screen.NewApps.route) {
            NewAppsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAppClick = { packageName ->
                    navController.navigate(Screen.AppDetail.create(packageName))
                }
            )
        }

        // ---------------- APP DETAIL ----------------
        composable(
            route = Screen.AppDetail.route,
            arguments = listOf(navArgument("pkg") { type = NavType.StringType })
        ) {
            val pkg = it.arguments?.getString("pkg")!!
            AppDetailScreen(
                packageName = pkg,
                onBack = { navController.popBackStack() }
            )
        }
    }
}