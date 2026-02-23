package com.example.permissionauditor.ui.screens.bottomnav

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.permissionauditor.AppApplication
import com.example.permissionauditor.ui.screens.bottomnav.bottom_screens.widget.loader.CircularScanner
import com.example.permissionauditor.ui.screens.bottomnav.bottom_screens.widget.loader.ScanStep
import com.example.permissionauditor.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.delay

@Composable
fun ScanningScreen(
    viewModel: DashboardViewModel = viewModel(),
    onScanComplete: () -> Unit,
    isRescan: Boolean = false
) {
    val context = LocalContext.current
    var progress by remember { mutableFloatStateOf(0f) }
    var completed by remember { mutableStateOf(false) }

    val allApps by viewModel.allAppsFromDb.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // 🔔 Notification permission launcher (Android 13+)
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            // Permission handled
        }

    // 🔐 Ask permission ONCE when screen opens (only on initial scan)
    LaunchedEffect(Unit) {
        if (!isRescan && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    // 🔄 Trigger scan when screen appears
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.scanAllApps(AppApplication.sessionManager.getSystemApplication)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 🚫 Disable back button during scan
    BackHandler(enabled = true) {}

    // ⏳ Animated scanning progress
    LaunchedEffect(Unit) {
        for (i in 0..100) {
            progress = i / 100f
            delay(40) // ~4 seconds total
        }
        completed = true
    }

    // 🚀 Navigate once scan finishes
    LaunchedEffect(completed, isLoading) {
        if (completed && !isLoading) {
            delay(500)
            onScanComplete()
        }
    }

    // ================= UI =================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {

            // ✨ Title
            Text(
                text = if (isRescan) "Refreshing..." else "Scanning Apps",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // ✨ Circular scanner with percentage
            Box(contentAlignment = Alignment.Center) {
                CircularScanner(progress = progress)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    AnimatedVisibility(
                        visible = completed,
                        enter = fadeIn() + scaleIn()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // ✨ App count card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Text(
                    text = if (allApps.isNotEmpty()) {
                        if (isRescan) "Rescanning ${allApps.size} apps"
                        else "Found ${allApps.size} apps"
                    } else {
                        "Discovering apps..."
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(16.dp))

            // ✨ Scan steps
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScanStep("Discovering apps", progress > 0.2f)
                ScanStep("Reading permissions", progress > 0.5f)
                ScanStep("Scoring risk", progress > 0.8f)
            }

            // ✨ Loading state indicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Processing...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}