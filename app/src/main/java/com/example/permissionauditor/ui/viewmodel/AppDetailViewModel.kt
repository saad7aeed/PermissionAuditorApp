package com.example.permissionauditor.ui.viewmodel

import android.Manifest
import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.permissionauditor.db.AppDatabase
import com.example.permissionauditor.ui.model.AuditedApp
import com.example.permissionauditor.ui.model.Bages
import com.example.permissionauditor.ui.model.PermissionCategory
import com.example.permissionauditor.ui.model.SpecialCapability
import com.example.permissionauditor.ui.model.repo.AuditedAppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Data class for detailed permissions
data class PermissionDetail(
    val permissionName: String,
    val permissionUse: String,
    val whyWeUseThis: String
)

/**
 * ViewModel for handling Audited Apps with Room + Compose
 */
class AppDetailViewModel(application: Application) : AndroidViewModel(application) {

    // Get DAO and Repository
    private val dao = AppDatabase.getInstance(application).auditedAppDao()
    private val repository = AuditedAppRepository(application, dao)

    // StateFlow for currently selected app
    private val _app = MutableStateFlow<AuditedApp?>(null)
    val app: StateFlow<AuditedApp?> = _app.asStateFlow()

    // Expose all apps from Room as StateFlow for Compose
    val allApps: StateFlow<List<AuditedApp>> = repository.observeApps()
        .map { list ->
            // Load icons dynamically (Drawable cannot be stored in Room)
            list.map { it.withIcon(application) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    init {
        // Initial scan on ViewModel creation
        viewModelScope.launch(Dispatchers.IO) {
            repository.initialScan()
        }
    }

    /**
     * Load a single app by package name from Room/Scanner
     */
    fun loadApp(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val scannedApp = repository.scanForNewApps().find { it.packageName == packageName }
                ?: dao.observeAll().first().find { it.packageName == packageName }
            _app.value = scannedApp?.withIcon(getApplication())
        }
    }

    /**
     * Rescan for new apps. Returns list of newly installed apps
     */
    fun rescanForNewApps(onNewApps: (List<AuditedApp>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val newApps = repository.scanForNewApps()
            if (newApps.isNotEmpty()) {
                // Update UI
                onNewApps(newApps.map { it.withIcon(getApplication()) })
            }
        }
    }

    /**
     * Map runtime permissions to badge icons for Compose
     */
    private val permissionToBadge: Map<String, Bages> = mapOf(
        Manifest.permission.ACCESS_FINE_LOCATION to
                Bages("Location", Icons.Filled.LocationOn, PermissionCategory.Location),
        Manifest.permission.ACCESS_BACKGROUND_LOCATION to
                Bages("Location", Icons.Filled.LocationOn, PermissionCategory.Location),
        Manifest.permission.CAMERA to
                Bages("Camera", Icons.Filled.CameraAlt, PermissionCategory.Camera),
        Manifest.permission.RECORD_AUDIO to
                Bages("Microphone", Icons.Filled.Mic, PermissionCategory.Microphone),
        Manifest.permission.READ_CONTACTS to
                Bages("Contacts", Icons.Filled.Person, PermissionCategory.Contacts),
        Manifest.permission.WRITE_CONTACTS to
                Bages("Contacts", Icons.Filled.Person, PermissionCategory.Contacts),
        Manifest.permission.READ_SMS to
                Bages("Sms/Phone", Icons.Filled.Phone, PermissionCategory.Phone),
        Manifest.permission.SEND_SMS to
                Bages("Sms/Phone", Icons.Filled.Phone, PermissionCategory.Phone)
    )

    /**
     * Map runtime permissions to detailed info for Compose
     */
    private val permissionDetailsMap = mapOf(
        Manifest.permission.ACCESS_FINE_LOCATION to PermissionDetail(
            permissionName = "Location",
            permissionUse = "Access precise location of the device",
            whyWeUseThis = "Used to provide location-based security insights and risk analysis"
        ),
        Manifest.permission.ACCESS_BACKGROUND_LOCATION to PermissionDetail(
            permissionName = "Location (Background)",
            permissionUse = "Access device location even when app is in the background",
            whyWeUseThis = "Used to detect apps tracking location without user actively using them"
        ),
        Manifest.permission.CAMERA to PermissionDetail(
            permissionName = "Camera",
            permissionUse = "Capture photos and record videos using the camera",
            whyWeUseThis = "Used to alert if an app can potentially spy or capture visual content"
        ),
        Manifest.permission.RECORD_AUDIO to PermissionDetail(
            permissionName = "Microphone",
            permissionUse = "Record audio from the device microphone",
            whyWeUseThis = "Used to detect apps that can listen to conversations or sounds"
        ),
        Manifest.permission.READ_CONTACTS to PermissionDetail(
            permissionName = "Contacts",
            permissionUse = "Read user contacts stored on the device",
            whyWeUseThis = "Used to detect apps that can access personal contact information"
        ),
        Manifest.permission.WRITE_CONTACTS to PermissionDetail(
            permissionName = "Contacts",
            permissionUse = "Modify or add contacts on the device",
            whyWeUseThis = "Used to detect apps that can alter or create contact information"
        ),
        Manifest.permission.READ_SMS to PermissionDetail(
            permissionName = "SMS",
            permissionUse = "Read text messages from the device",
            whyWeUseThis = "Used to detect apps that can access private messages"
        ),
        Manifest.permission.SEND_SMS to PermissionDetail(
            permissionName = "SMS",
            permissionUse = "Send text messages from the device",
            whyWeUseThis = "Used to detect apps that can send messages without user consent"
        )
    )

    /**
     * Returns a list of badges (permission icons + names) for the currently loaded app
     */
    fun getBadges(): List<Bages> {
        val currentApp = _app.value ?: return emptyList()
        val badges = mutableSetOf<Bages>()

        // Permissions badges
        currentApp.permissions.forEach { perm ->
            permissionToBadge[perm.name]?.let { badges.add(it) }
        }

        // Special capabilities badges
        currentApp.specialCapabilities.forEach { cap ->
            when (cap) {
                SpecialCapability.Accessibility -> badges.add(
                    Bages("Accessibility", Icons.Filled.Warning, PermissionCategory.Accessibility)
                )

                SpecialCapability.NotificationListener -> badges.add(
                    Bages(
                        "Notification",
                        Icons.Filled.Warning,
                        PermissionCategory.NotificationListener
                    )
                )

                SpecialCapability.DeviceAdmin -> badges.add(
                    Bages("Device Admin", Icons.Filled.Warning, PermissionCategory.DeviceAdmin)
                )

                SpecialCapability.Vpn -> badges.add(
                    Bages("VPN", Icons.Filled.Warning, PermissionCategory.VPN)
                )
            }
        }
        return badges.toList()
    }

    /**
     * Returns a list of PermissionDetail for the currently loaded app
     */
    fun getPermissionDetails(): List<PermissionDetail> {
        val currentApp = _app.value ?: return emptyList()
        val details = currentApp.permissions.mapNotNull { perm ->
            permissionDetailsMap[perm.name]
        }.toMutableList()

        // Add special capabilities as pseudo-permissions
        currentApp.specialCapabilities.forEach { cap ->
            when (cap) {
                SpecialCapability.Accessibility -> details.add(
                    PermissionDetail(
                        permissionName = "Accessibility",
                        permissionUse = "Control UI and read screen content",
                        whyWeUseThis = "Used to detect apps that can interact with or monitor other apps"
                    )
                )

                SpecialCapability.NotificationListener -> details.add(
                    PermissionDetail(
                        permissionName = "Notification Listener",
                        permissionUse = "Read notifications from other apps",
                        whyWeUseThis = "Used to detect apps reading personal notifications"
                    )
                )

                SpecialCapability.DeviceAdmin -> details.add(
                    PermissionDetail(
                        permissionName = "Device Admin",
                        permissionUse = "Change system settings and lock device",
                        whyWeUseThis = "Used to detect apps with full device control"
                    )
                )

                SpecialCapability.Vpn -> details.add(
                    PermissionDetail(
                        permissionName = "VPN",
                        permissionUse = "Route device network traffic through app",
                        whyWeUseThis = "Used to detect apps that can intercept or monitor network traffic"
                    )
                )
            }
        }

        return details
    }
}

/**
 * Extension to load app icon dynamically (Drawable is not stored in Room)
 */
fun AuditedApp.withIcon(context: Application): AuditedApp {
    val icon = try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (e: Exception) {
        null
    }
    return this.copy(icon = icon)
}
