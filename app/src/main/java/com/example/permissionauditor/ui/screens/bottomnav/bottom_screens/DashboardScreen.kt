package com.example.permissionauditor.ui.screens.bottomnav.bottom_screens

import SearchBar
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.permissionauditor.AppApplication
import com.example.permissionauditor.R
import com.example.permissionauditor.ui.model.PermissionCategory
import com.example.permissionauditor.ui.model.RiskBand
import com.example.permissionauditor.ui.screens.bottomnav.bottom_screens.widget.AppCard
import com.example.permissionauditor.ui.screens.bottomnav.bottom_screens.widget.RiskBadge
import com.example.permissionauditor.ui.screens.bottomnav.bottom_screens.widget.getPermissionCategoriesIconsList
import com.example.permissionauditor.ui.theme.air_orange200
import com.example.permissionauditor.ui.theme.air_red
import com.example.permissionauditor.ui.theme.alert
import com.example.permissionauditor.ui.theme.blue
import com.example.permissionauditor.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onAppClick: (String) -> Unit,
    onReloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredApps by viewModel.filteredApps.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val selectedRisk by viewModel.selectedRisk.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val selectedStatCard by viewModel.selectedStatCard.collectAsState() // ✅ Add this line

    // ✨ Animation states
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // 🔄 Scan on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.scanAllApps(AppApplication.sessionManager.getSystemApplication)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            // ✨ Modern gradient header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(vertical = 20.dp, horizontal = 16.dp)
                ) {
                    Column {
                        // Title with emoji
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            Column {
                                Text(
                                    text = "Dashboard",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${filteredApps.size} apps monitored",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            // ✨ Animated FAB with pulse effect
            var isPulsing by remember { mutableStateOf(true) }
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isPulsing) 1.1f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            FloatingActionButton(
                onClick = {
                    isPulsing = false
                    onReloadClick()
                },
                modifier = Modifier.scale(scale),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rescan",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
        ) {
            // ✨ Search Bar
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically(),
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        onSearch = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp)
                    )
                }
            }

            // ✨ Stats Cards with animation
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically(
                        animationSpec = tween(400, delayMillis = 100)
                    )
                )
                {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        // First row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernStatsCard(
                                icon = painterResource(id = R.drawable.warning),
                                count = viewModel.accessibilityCount.collectAsState().value,
                                label = "Special Access",
                                color = air_red,
                                modifier = Modifier.weight(1f),
                                isSelected = selectedStatCard == PermissionCategory.Accessibility, // ✅
                                onClick = { viewModel.toggleCategory(PermissionCategory.Accessibility) }
                            )
                            ModernStatsCard(
                                icon = painterResource(id = R.drawable.pin),
                                count = viewModel.locationCount.collectAsState().value,
                                label = "Location",
                                color = blue,
                                modifier = Modifier.weight(1f),
                                isSelected = selectedStatCard == PermissionCategory.Location, // ✅
                                onClick = { viewModel.toggleCategory(PermissionCategory.Location) }
                            )
                        }

                        // Second row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernStatsCard(
                                icon = painterResource(id = R.drawable.mic),
                                count = viewModel.cameraCount.collectAsState().value,
                                label = "Mic/Camera",
                                color = air_orange200,
                                modifier = Modifier.weight(1f),
                                isSelected = selectedStatCard == PermissionCategory.Camera ||
                                        selectedStatCard == PermissionCategory.Microphone, // ✅
                                onClick = {
                                    viewModel.toggleCategories(
                                        setOf(
                                            PermissionCategory.Camera,
                                            PermissionCategory.Microphone
                                        )
                                    )
                                }
                            )
                            ModernStatsCard(
                                icon = painterResource(id = R.drawable.shield),
                                count = viewModel.adminCount.collectAsState().value,
                                label = "Admin Access",
                                color = alert,
                                modifier = Modifier.weight(1f),
                                isSelected = selectedStatCard == PermissionCategory.DeviceAdmin, // ✅
                                onClick = { viewModel.toggleCategory(PermissionCategory.DeviceAdmin) }
                            )
                        }
                    }
                }
            }

            // ✨ Permission Category Filters
            // ✨ Permission Category Filters
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically(
                        animationSpec = tween(400, delayMillis = 200)
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Filter by Permission",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(scrollState),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            getPermissionCategoriesIconsList().forEach { badge ->
                                val isSelected = badge.category in selectedCategories
                                ModernChip(
                                    label = badge.name,
                                    icon = badge.img,
                                    isSelected = isSelected,
                                    onClick = { viewModel.toggleCategoryFilter(badge.category) } // ✅ Changed
                                )
                            }
                        }
                    }
                }
            }

            // ✨ Risk Filters Header
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically(
                        animationSpec = tween(400, delayMillis = 300)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Applications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${filteredApps.size} apps found",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RiskBadge(
                                label = "High",
                                selected = selectedRisk == RiskBand.High
                            ) { viewModel.toggleRisk(RiskBand.High) }

                            RiskBadge(
                                label = "Med",
                                selected = selectedRisk == RiskBand.Medium
                            ) { viewModel.toggleRisk(RiskBand.Medium) }

                            RiskBadge(
                                label = "Low",
                                selected = selectedRisk == RiskBand.Low
                            ) { viewModel.toggleRisk(RiskBand.Low) }
                        }
                    }
                }
            }

            // ✨ Apps List
            if (filteredApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "Scanning apps...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredApps,
                    key = { it.packageName }
                ) { app ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        AppCard(
                            app = app,
                            onClick = onAppClick,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ✨ Modern Stats Card Component
@Composable
private fun ModernStatsCard(
    icon: Painter,
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false, // ✅ Add parameter
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer // ✅ Highlighted
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp, // ✅ More elevation
            pressedElevation = 8.dp
        ),
        border = if (isSelected) { // ✅ Border when selected
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = icon,
                    contentDescription = "",
                    modifier = Modifier.size(34.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary // ✅ Highlighted text
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}

// ✨ Modern Chip Component
@Composable
private fun ModernChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        contentColor = contentColor,
        border = if (isSelected) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

fun openAppSettings(context: Context, pkg: String) {
    context.startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", pkg, null)
        )
    )
}