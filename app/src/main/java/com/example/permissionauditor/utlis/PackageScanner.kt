package com.example.permissionauditor.utlis

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import com.example.permissionauditor.ui.model.AppPermission
import com.example.permissionauditor.ui.model.AuditedApp
import com.example.permissionauditor.ui.model.Bages
import com.example.permissionauditor.ui.model.SpecialCapability
import com.example.permissionauditor.ui.model.PermissionCategory
import com.example.permissionauditor.ui.model.RiskBand

class PackageScanner(private val context: Context) {

    private val pm = context.packageManager

    // ✅ NEW: Resolves permissions with their granted status
    private fun resolvePermissions(pi: PackageInfo): List<AppPermission> {
        val requested = pi.requestedPermissions ?: return emptyList()
        val flags = pi.requestedPermissionsFlags ?: IntArray(requested.size)

        return requested.mapIndexed { index, permName ->
            val isGranted = (flags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            AppPermission(name = permName, isGranted = isGranted)
        }
    }

    fun getLaunchableApps(): List<AuditedApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfos.mapNotNull { resolveInfo ->
            try {
                val pkgName = resolveInfo.activityInfo.packageName
                val pi = pm.getPackageInfo(
                    pkgName,
                    PackageManager.GET_PERMISSIONS or
                            PackageManager.GET_SERVICES or
                            PackageManager.GET_RECEIVERS
                )

                val label = resolveInfo.loadLabel(pm).toString()
                val icon = resolveInfo.loadIcon(pm)
                val permissions = resolvePermissions(pi) // ✅ UPDATED
                val specialCaps = detectCapabilities(pi)
                val (score, band) = RiskScorer.calculate(permissions, specialCaps)

                AuditedApp(
                    packageName = pkgName,
                    appLabel = label,
                    icon = icon,
                    permissions = permissions,
                    specialCapabilities = specialCaps,
                    riskScore = score,
                    riskBand = band
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getAllApps(includeSystemApps: Boolean = true): List<AuditedApp> {
        val packages = pm.getInstalledPackages(
            PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS
        )

        return packages.mapNotNull { pkg ->
            try {
                val appInfo = pkg.applicationInfo ?: return@mapNotNull null

                val isSystemApp = appInfo.isSystemOrUpdatedSystemApp()

                if (!includeSystemApps && isSystemApp) return@mapNotNull null

                val label = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                val permissions = resolvePermissions(pkg) // ✅ UPDATED
                val specialCaps = detectCapabilities(pkg)
                val (score, band) = RiskScorer.calculate(permissions, specialCaps)

                AuditedApp(
                    packageName = pkg.packageName,
                    appLabel = label,
                    icon = icon,
                    permissions = permissions,
                    specialCapabilities = specialCaps,
                    riskScore = score,
                    riskBand = band,
                    isSystemApp = isSystemApp
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun detectCapabilities(pkg: PackageInfo): List<SpecialCapability> {
        val caps = mutableListOf<SpecialCapability>()
        pkg.services?.forEach {
            when (it.permission) {
                "android.permission.BIND_ACCESSIBILITY_SERVICE" -> caps += SpecialCapability.Accessibility
                "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" -> caps += SpecialCapability.NotificationListener
                "android.permission.BIND_VPN_SERVICE" -> caps += SpecialCapability.Vpn
            }
        }
        pkg.receivers?.forEach {
            if (it.permission == "android.permission.BIND_DEVICE_ADMIN") caps += SpecialCapability.DeviceAdmin
        }
        return caps
    }

    fun getAppByPackageName(packageName: String): AuditedApp? {
        return try {
            val pi = pm.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_SERVICES or
                        PackageManager.GET_RECEIVERS
            )

            val appInfo = pi.applicationInfo ?: return null
            val label = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)
            val permissions = resolvePermissions(pi) // ✅ UPDATED
            val specialCaps = detectCapabilities(pi)
            val (score, band) = RiskScorer.calculate(permissions, specialCaps)

            AuditedApp(
                packageName = packageName,
                appLabel = label,
                icon = icon,
                permissions = permissions,
                specialCapabilities = specialCaps,
                riskScore = score,
                riskBand = band
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getBadgesForPackage(packageName: String): List<Bages> {
        val app = getAppByPackageName(packageName) ?: return emptyList()
        val badges = mutableSetOf<Bages>()

        // ✅ UPDATED: access appPerm.name instead of raw string
        app.permissions.forEach { appPerm ->
            permissionToBadge[appPerm.name]?.let { badges.add(it) }
        }

        app.specialCapabilities.forEach { cap ->
            when (cap) {
                SpecialCapability.Accessibility ->
                    badges.add(Bages("Accessibility", Icons.Filled.Warning, PermissionCategory.Accessibility))
                SpecialCapability.NotificationListener ->
                    badges.add(Bages("Notification", Icons.Filled.Warning, PermissionCategory.NotificationListener))
                SpecialCapability.DeviceAdmin ->
                    badges.add(Bages("Device Admin", Icons.Filled.Warning, PermissionCategory.DeviceAdmin))
                SpecialCapability.Vpn ->
                    badges.add(Bages("VPN", Icons.Filled.Warning, PermissionCategory.VPN))
            }
        }

        return badges.toList()
    }

    private val permissionToBadge: Map<String, Bages> = mapOf(
        Manifest.permission.ACCESS_FINE_LOCATION to
                Bages("Location", Icons.Outlined.LocationOn, PermissionCategory.Location),
        Manifest.permission.ACCESS_BACKGROUND_LOCATION to
                Bages("Location", Icons.Outlined.LocationOn, PermissionCategory.Location),
        Manifest.permission.CAMERA to
                Bages("Camera", Icons.Outlined.CameraAlt, PermissionCategory.Camera),
        Manifest.permission.RECORD_AUDIO to
                Bages("Microphone", Icons.Outlined.Mic, PermissionCategory.Microphone),
        Manifest.permission.READ_CONTACTS to
                Bages("Contacts", Icons.Outlined.Person, PermissionCategory.Contacts),
        Manifest.permission.WRITE_CONTACTS to
                Bages("Contacts", Icons.Outlined.Person, PermissionCategory.Contacts),
        Manifest.permission.READ_SMS to
                Bages("Sms/Phone", Icons.Outlined.Phone, PermissionCategory.Phone),
        Manifest.permission.SEND_SMS to
                Bages("Sms/Phone", Icons.Outlined.Phone, PermissionCategory.Phone)
    )

    private fun ApplicationInfo.isSystemOrUpdatedSystemApp(): Boolean {
        return (flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }
}

// ✅ UPDATED: RiskScorer now accepts List<AppPermission>
object RiskScorer {
    fun calculate(permissions: List<AppPermission>, caps: List<SpecialCapability>): Pair<Int, RiskBand> {
        // Only count permissions that are actually granted
        val grantedPerms = permissions.filter { it.isGranted }.map { it.name }

        var score = 0
        if (SpecialCapability.Accessibility in caps) score += 45
        if (SpecialCapability.NotificationListener in caps) score += 35
        if (SpecialCapability.DeviceAdmin in caps) score += 35
        if (Manifest.permission.SYSTEM_ALERT_WINDOW in grantedPerms) score += 30
        if (Manifest.permission.ACCESS_BACKGROUND_LOCATION in grantedPerms) score += 30
        if (Manifest.permission.ACCESS_FINE_LOCATION in grantedPerms) score += 20
        if (Manifest.permission.RECORD_AUDIO in grantedPerms) score += 20
        if (Manifest.permission.CAMERA in grantedPerms) score += 15

        val band = when {
            score >= 50 -> RiskBand.High
            score >= 20 -> RiskBand.Medium
            else -> RiskBand.Low
        }
        return score to band
    }
}