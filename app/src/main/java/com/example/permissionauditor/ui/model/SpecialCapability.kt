package com.example.permissionauditor.ui.model

import android.Manifest
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.vector.ImageVector

// Your existing data classes
//data class AuditedApp(
//    val packageName: String,
//    val appLabel: String,
//    val icon: Drawable?,
//    val permissions: List<String>,
//    val specialCapabilities: List<SpecialCapability>,
//    val riskScore: Int,
//    val riskBand: RiskBand,
//    val isSystemApp: Boolean = false // 👈 Add this
//)

// Enums
enum class SpecialCapability { Accessibility, NotificationListener, DeviceAdmin, Vpn }
enum class RiskBand { Low, Medium, High }
enum class PermissionCategory { Location, Camera, Microphone, Contacts, Email, Phone, Accessibility, NotificationListener, DeviceAdmin, VPN, Warning }

// Your other data classes
data class Bages(val name: String, val img: ImageVector, val category: PermissionCategory)
data class PermissionDetail(
    val permissionName: String,
    val permissionUse: String,
    val whyWeUseThis: String
)

// ✅ Put the extension function here
fun AuditedApp.permissionCategories(): Set<PermissionCategory> {
    val categories = mutableSetOf<PermissionCategory>()
    permissions.forEach { perm ->
        when (perm.name) {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION -> categories.add(
                PermissionCategory.Location
            )

            Manifest.permission.CAMERA -> categories.add(PermissionCategory.Camera)
            Manifest.permission.RECORD_AUDIO
                -> categories.add(PermissionCategory.Microphone)

            Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS -> categories.add(
                PermissionCategory.Contacts
            )

            Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS -> categories.add(
                PermissionCategory.Phone
            )
        }
    }
    specialCapabilities.forEach { cap ->
        when (cap) {
            SpecialCapability.Accessibility -> categories.add(PermissionCategory.Accessibility)
            SpecialCapability.NotificationListener -> categories.add(PermissionCategory.NotificationListener)
            SpecialCapability.DeviceAdmin -> categories.add(PermissionCategory.DeviceAdmin)
            SpecialCapability.Vpn -> categories.add(PermissionCategory.VPN)
        }
    }
    return categories
}
