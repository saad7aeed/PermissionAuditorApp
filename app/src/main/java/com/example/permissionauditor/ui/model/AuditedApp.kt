package com.example.permissionauditor.ui.model


import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

// Base entity for audited_apps table
@Entity(tableName = "audited_apps")
data class AuditedApp(
    @PrimaryKey
    val packageName: String,
    val appLabel: String,
    @Ignore
    val icon: Drawable? = null,
    val permissions: List<AppPermission>,
    val specialCapabilities: List<SpecialCapability>,
    val riskScore: Int,
    val riskBand: RiskBand,
    val isSystemApp: Boolean = false
) {
    constructor(
        packageName: String,
        appLabel: String,
        permissions: List<AppPermission>,
        specialCapabilities: List<SpecialCapability>,
        riskScore: Int,
        riskBand: RiskBand,
        isSystemApp: Boolean
    ) : this(packageName, appLabel, null, permissions, specialCapabilities, riskScore, riskBand, isSystemApp)
}

// Entity for previous_apps table (snapshot)
@Entity(tableName = "previous_apps")
data class PreviousApp(
    @PrimaryKey
    val packageName: String,
    val appLabel: String,
    @Ignore
    val icon: Drawable? = null,
    val permissions: List<AppPermission>,
    val specialCapabilities: List<SpecialCapability>,
    val riskScore: Int,
    val riskBand: RiskBand,
    val isSystemApp: Boolean = false
) {
    constructor(
        packageName: String,
        appLabel: String,
        permissions: List<AppPermission>,
        specialCapabilities: List<SpecialCapability>,
        riskScore: Int,
        riskBand: RiskBand,
        isSystemApp: Boolean
    ) : this(packageName, appLabel, null, permissions, specialCapabilities, riskScore, riskBand, isSystemApp)
}

// Extension functions for conversion
fun AuditedApp.toPreviousApp() = PreviousApp(
    packageName = packageName,
    appLabel = appLabel,
    icon = icon,
    permissions = permissions,
    specialCapabilities = specialCapabilities,
    riskScore = riskScore,
    riskBand = riskBand,
    isSystemApp = isSystemApp
)

fun PreviousApp.toAuditedApp() = AuditedApp(
    packageName = packageName,
    appLabel = appLabel,
    icon = icon,
    permissions = permissions,
    specialCapabilities = specialCapabilities,
    riskScore = riskScore,
    riskBand = riskBand,
    isSystemApp = isSystemApp
)

data class AppPermission(
    val name: String,
    val isGranted: Boolean
)