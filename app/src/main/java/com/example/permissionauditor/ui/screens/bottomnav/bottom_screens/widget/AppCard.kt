package com.example.permissionauditor.ui.screens.bottomnav.bottom_screens.widget

import android.Manifest
import android.widget.ImageView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.permissionauditor.AppApplication
import com.example.permissionauditor.R
import com.example.permissionauditor.ui.model.AppPermission
import com.example.permissionauditor.ui.model.AuditedApp
import com.example.permissionauditor.ui.model.Bages
import com.example.permissionauditor.ui.model.PermissionCategory
import com.example.permissionauditor.ui.model.RiskBand

@Composable
fun AppCard(
    app: AuditedApp,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        onClick = {
            isPressed = true
            onClick(app.packageName)
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        border = BorderStroke(
            width = 1.dp,
            color = when (app.riskBand) {
                RiskBand.High -> Color(0xFFF44336).copy(alpha = 0.3f)
                RiskBand.Medium -> Color(0xFFFFC107).copy(alpha = 0.3f)
                RiskBand.Low -> Color(0xFF4CAF50).copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App Icon
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    if (app.icon != null) {
                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                }
                            },
                            update = { view ->
                                view.setImageDrawable(app.icon)
                                view.contentDescription = app.appLabel
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // App Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = app.appLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (AppApplication.sessionManager.getPrivacy) {
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // ✅ Permission Icons Row with granted/denied color coding
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        val permissionIcons = getPermissionCategoriesIcons(app)

                        if (permissionIcons.isEmpty()) {
                            Text(
                                text = "No sensitive permissions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        } else {
                            permissionIcons.take(5).forEach { (icon, isGranted) ->
                                // ✅ Green if granted, Red if not granted
                                val tintColor = if (isGranted) Color(0xFF4CAF50) else Color(0xFFF44336)
                                val bgColor = tintColor.copy(alpha = 0.1f)

                                Surface(
                                    shape = CircleShape,
                                    color = bgColor,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(16.dp),
                                        tint = tintColor // ✅ Color reflects grant status
                                    )
                                }
                            }

                            if (permissionIcons.size > 5) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = "+${permissionIcons.size - 5}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ModernRiskBadge(
                riskBand = app.riskBand,
                riskScore = app.riskScore
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun ModernRiskBadge(
    riskBand: RiskBand,
    riskScore: Int
) {
    val (bgColor, contentColor, label, icon) = when (riskBand) {
        RiskBand.Low -> RiskConfig(
            bgColor = Color(0xFF4CAF50),
            contentColor = Color.White,
            label = "Low",
            icon = R.drawable.low_risk
        )
        RiskBand.Medium -> RiskConfig(
            bgColor = Color(0xFFFFC107),
            contentColor = Color(0xFF1A1A1A),
            label = "Med",
            icon = R.drawable.medium_risk
        )
        RiskBand.High -> RiskConfig(
            bgColor = Color(0xFFF44336),
            contentColor = Color.White,
            label = "High",
            icon = R.drawable.high_risk
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = bgColor,
            modifier = Modifier.size(40.dp),
            shadowElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = bgColor,
            fontSize = 10.sp
        )
    }
}

private data class RiskConfig(
    val bgColor: Color,
    val contentColor: Color,
    val label: String,
    val icon: Int
)

@Composable
fun RiskBadge(
    riskBand: RiskBand,
    riskScore: Int
) {
    val (bgColor, contentColor, iconRes) = when (riskBand) {
        RiskBand.Low -> Triple(
            Color(0xFF4CAF50),
            Color.White,
            painterResource(id = R.drawable.low_risk)
        )
        RiskBand.Medium -> Triple(
            Color(0xFFFFC107),
            Color.Black,
            painterResource(id = R.drawable.medium_risk)
        )
        RiskBand.High -> Triple(
            Color(0xFFF44336),
            Color.White,
            painterResource(id = R.drawable.high_risk)
        )
    }

    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = iconRes,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
    }
}

// ✅ UPDATED: Returns List<Pair<ImageVector, Boolean>>
// where Boolean = isGranted (true = green, false = red)
fun getPermissionCategoriesIcons(app: AuditedApp): List<Pair<ImageVector, Boolean>> {
    val icons = mutableListOf<Pair<ImageVector, Boolean>>()
    val perms = app.permissions // List<AppPermission>

    // Helper: find if a permission matching the predicate exists, and if so, is it granted?
    fun check(predicate: (String) -> Boolean): Boolean? {
        val match = perms.firstOrNull { predicate(it.name) }
        return match?.isGranted
    }

    check { it.contains("LOCATION") }?.let { icons += Icons.Outlined.LocationOn to it }
    check { it.contains("CAMERA") }?.let { icons += Icons.Outlined.CameraAlt to it }
    check { it.contains("RECORD_AUDIO") }?.let { icons += Icons.Outlined.Mic to it }
    check { it.contains("CONTACTS") }?.let { icons += Icons.Outlined.Person to it }
    check { it.contains("SMS") }?.let { icons += Icons.Outlined.Email to it }
    check { it.contains("PHONE") }?.let { icons += Icons.Outlined.Phone to it }
    check { it == Manifest.permission.SYSTEM_ALERT_WINDOW }?.let { icons += Icons.Outlined.Warning to it }

    return icons
}

fun getPermissionCategoriesIconsList(): List<Bages> = listOf(
    Bages("Location", Icons.Outlined.LocationOn, PermissionCategory.Location),
    Bages("Camera", Icons.Outlined.CameraAlt, PermissionCategory.Camera),
    Bages("Microphone", Icons.Outlined.Mic, PermissionCategory.Microphone),
    Bages("Contacts", Icons.Outlined.Person, PermissionCategory.Contacts),
    Bages("Sms/Phone", Icons.Outlined.Phone, PermissionCategory.Phone),
)