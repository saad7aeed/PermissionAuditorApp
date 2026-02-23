package com.example.permissionauditor.ui.viewmodel

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.permissionauditor.AppApplication
import com.example.permissionauditor.MainActivity
import com.example.permissionauditor.R
import com.example.permissionauditor.db.AppDatabase
import com.example.permissionauditor.ui.model.AuditedApp
import com.example.permissionauditor.ui.model.PermissionCategory
import com.example.permissionauditor.ui.model.RiskBand
import com.example.permissionauditor.ui.model.SpecialCapability
import com.example.permissionauditor.ui.model.permissionCategories
import com.example.permissionauditor.ui.model.toPreviousApp
import com.example.permissionauditor.utlis.PackageScanner
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val scanner = PackageScanner(application)
    private val db = AppDatabase.getInstance(application)
    private val packageManager = application.packageManager

    // ✅ Loading state for scanning
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ✅ NEW: Newly installed apps since last snapshot
    private val _newlyInstalledApps = MutableStateFlow<List<AuditedApp>>(emptyList())
    val newlyInstalledApps: StateFlow<List<AuditedApp>> = _newlyInstalledApps.asStateFlow()

    // ✅ NEW: Navigation event for new apps detected
    private val _navigateToNewApps = MutableSharedFlow<Boolean>()
    val navigateToNewApps: SharedFlow<Boolean> = _navigateToNewApps.asSharedFlow()

    // ✅ Icon cache for better performance
    private val iconCache = mutableMapOf<String, Drawable?>()

    // Add this after line 108 (after _selectedCategories)
    private val _selectedStatCard = MutableStateFlow<PermissionCategory?>(null)
    val selectedStatCard: StateFlow<PermissionCategory?> = _selectedStatCard.asStateFlow()

    // -----------------------------
    // 1️⃣ All apps from Room DB as Flow
    // -----------------------------
    val allAppsFromDb: StateFlow<List<AuditedApp>> =
        db.auditedAppDao().observeAll()
            .map { apps ->
                apps.map { app ->
                    if (app.icon == null) {
                        app.copy(icon = loadAppIcon(app.packageName))
                    } else {
                        app
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // -----------------------------
    // ✅ Load app icon from PackageManager
    // -----------------------------
    private fun loadAppIcon(packageName: String): Drawable? {
        if (iconCache.containsKey(packageName)) {
            return iconCache[packageName]
        }

        return try {
            val icon = packageManager.getApplicationIcon(packageName)
            iconCache[packageName] = icon
            icon
        } catch (e: Exception) {
            null
        }
    }

    // -----------------------------
    // ✅ Preload icons for visible apps
    // -----------------------------
    fun preloadIcons(apps: List<AuditedApp>) {
        viewModelScope.launch(Dispatchers.IO) {
            apps.forEach { app ->
                if (app.icon == null && !iconCache.containsKey(app.packageName)) {
                    loadAppIcon(app.packageName)
                }
            }
        }
    }

    // -----------------------------
    // 2️⃣ Search query
    // -----------------------------
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // -----------------------------
    // 3️⃣ Risk filter
    // -----------------------------
    private val _selectedRisk = MutableStateFlow<RiskBand?>(null)
    val selectedRisk: StateFlow<RiskBand?> = _selectedRisk.asStateFlow()

    fun toggleRisk(risk: RiskBand) {
        _selectedRisk.value = if (_selectedRisk.value == risk) null else risk
    }

    // -----------------------------
    // 4️⃣ System apps toggle
    // -----------------------------
    private val _showSystemApps = MutableStateFlow(true)
    val showSystemApps: StateFlow<Boolean> = _showSystemApps.asStateFlow()

    fun setShowSystemApps(show: Boolean) {
        _showSystemApps.value = show
    }

    // -----------------------------
    // 5️⃣ Selected permission categories
    // -----------------------------
    private val _selectedCategories = MutableStateFlow<Set<PermissionCategory>>(emptySet())
    val selectedCategories: StateFlow<Set<PermissionCategory>> = _selectedCategories.asStateFlow()

    fun toggleCategory(category: PermissionCategory) {
        val current = _selectedCategories.value.toMutableSet()

        // Check if this stat card is currently selected
        if (_selectedStatCard.value == category) {
            // If clicking the same card, deselect it
            current.remove(category)
            _selectedStatCard.value = null
        } else {
            // If clicking a different card, select it
            // Clear ALL previous selections first
            current.clear() // ✅ Changed: clear everything first
            // Then add only the new category
            current.add(category)
            _selectedStatCard.value = category
        }

        _selectedCategories.value = current
    }

    fun toggleCategories(categories: Set<PermissionCategory>) {
        val current = _selectedCategories.value.toMutableSet()

        // Check if any of these categories are currently selected as stat card
        val isCurrentlySelected = categories.any { it == _selectedStatCard.value }

        if (isCurrentlySelected) {
            // If clicking the same card, deselect it
            categories.forEach { current.remove(it) }
            _selectedStatCard.value = null
        } else {
            // If clicking a different card, select it
            // Clear ALL previous selections first
            current.clear() // ✅ Changed: clear everything first
            // Then add only the new categories
            categories.forEach { current.add(it) }
            _selectedStatCard.value = categories.firstOrNull()
        }

        _selectedCategories.value = current
    }

    // Add this new function after toggleCategories
    fun toggleCategoryFilter(category: PermissionCategory) {
        val current = _selectedCategories.value.toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        _selectedCategories.value = current

        // Clear stat card selection when manually using filters
        _selectedStatCard.value = null
    }

    // -----------------------------
    // 6️⃣ Filtered apps
    // -----------------------------
    val filteredApps: StateFlow<List<AuditedApp>> = combine(
        allAppsFromDb,
        _searchQuery,
        _selectedCategories,
        _selectedRisk,
        _showSystemApps
    ) { apps, query, categories, risk, showSystem ->
        apps
            .filter { it.appLabel.contains(query, ignoreCase = true) }
            .filter { app ->
                categories.isEmpty() || app.permissionCategories().any { it in categories }
            }
            .filter { app -> risk == null || app.riskBand == risk }
            .filter { app -> showSystem || !app.isSystemApp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // -----------------------------
    // 7️⃣ Stats
    // -----------------------------
    val appsScannedCount: StateFlow<Int> =
        allAppsFromDb.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val accessibilityCount: StateFlow<Int> =
        allAppsFromDb.map { list ->
            list.count { SpecialCapability.Accessibility in it.specialCapabilities }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val locationCount: StateFlow<Int> =
        allAppsFromDb.map { list ->
            list.count { app ->
                app.permissions.any {
                    it.name == Manifest.permission.ACCESS_BACKGROUND_LOCATION ||
                            it.name == Manifest.permission.ACCESS_FINE_LOCATION
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val cameraCount: StateFlow<Int> =
        allAppsFromDb.map { list ->
            list.count { app ->
                app.permissions.any {
                    it.name == Manifest.permission.CAMERA ||
                            it.name == Manifest.permission.RECORD_AUDIO
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val adminCount: StateFlow<Int> =
        allAppsFromDb.map { list ->
            list.count { SpecialCapability.DeviceAdmin in it.specialCapabilities }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // -----------------------------
    // 8️⃣ Initial scan on first launch
    // -----------------------------
    init {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            try {
                // ✅ Use saved preference instead of hardcoded true
                val includeSystem = AppApplication.sessionManager.getSystemApplication
                val apps = scanner.getAllApps(includeSystemApps = includeSystem)
                val appsWithIcons = apps.map { app ->
                    app.copy(icon = loadAppIcon(app.packageName))
                }

                db.auditedAppDao().insertAll(appsWithIcons)

                // ✅ Take snapshot on first scan
                if (!AppApplication.sessionManager.isFirstScanDone) {
                    takeSnapshot(appsWithIcons)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // -----------------------------
    // ✅ NEW: Take snapshot of current apps
    // -----------------------------
    private suspend fun takeSnapshot(apps: List<AuditedApp>) {
        val previousApps = apps.map { it.toPreviousApp() }
        db.previousAppDao().deleteAllApps()
        db.previousAppDao().insertAll(previousApps)
        AppApplication.sessionManager.markSnapshotTaken()
    }

    // -----------------------------
    // ✅ NEW: Compare current apps with previous snapshot
    // -----------------------------
    private suspend fun compareWithSnapshot(currentApps: List<AuditedApp>): List<AuditedApp> {
        val previousPackages = db.previousAppDao().getAllPackages().toSet()
        return currentApps.filter { it.packageName !in previousPackages }
    }

    // -----------------------------
    // 9️⃣ Scan all apps with snapshot logic
    // -----------------------------
    fun scanAllApps(showSystemApps: Boolean = AppApplication.sessionManager.getSystemApplication) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            try {
                // Get current installed apps
                val currentApps = scanner.getAllApps(includeSystemApps = showSystemApps)
                    .sortedByDescending { it.riskScore }

                val appsWithIcons = currentApps.map { app ->
                    app.copy(icon = loadAppIcon(app.packageName))
                }

                // ✅ Compare with previous snapshot to find new apps
                val newApps = compareWithSnapshot(appsWithIcons)
                _newlyInstalledApps.value = newApps

                // Clear and insert new scan results
                db.auditedAppDao().deleteAllApps()
                db.auditedAppDao().insertAll(appsWithIcons)

                // ✅ Send notifications for new apps
                newApps.forEach { app ->
                  //  sendNotification(app)
                }

                // ✅ Take new snapshot if it's Monday or first scan
                if (AppApplication.sessionManager.shouldTakeSnapshot()) {
                    if (newApps.isNotEmpty()) {
                        sendNotification()
                        _navigateToNewApps.emit(true)
                    }
                    takeSnapshot(appsWithIcons)
                }

//                // ✅ NEW: Trigger navigation to NewAppsScreen if new apps found
//                if (newApps.isNotEmpty()) {
//                    sendNotification()
//                    _navigateToNewApps.emit(true)
//                }

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun scanNewInstallApps(showSystemApps: Boolean = AppApplication.sessionManager.getSystemApplication) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            try {
                // Get current installed apps
                val currentApps = scanner.getAllApps(includeSystemApps = showSystemApps)
                    .sortedByDescending { it.riskScore }

                val appsWithIcons = currentApps.map { app ->
                    app.copy(icon = loadAppIcon(app.packageName))
                }

                // ✅ Compare with previous snapshot to find new apps
                val newApps = compareWithSnapshot(appsWithIcons)
                _newlyInstalledApps.value = newApps

                // Clear and insert new scan results
//                db.auditedAppDao().deleteAllApps()
//                db.auditedAppDao().insertAll(appsWithIcons)
//
//                // ✅ Send notifications for new apps
//                newApps.forEach { app ->
//                  //  sendNotification(app)
//                }
//
//                // ✅ Take new snapshot if it's Monday or first scan
//                if (AppApplication.sessionManager.shouldTakeSnapshot()) {
////                    if (newApps.isNotEmpty()) {
////                        sendNotification()
//////                        _navigateToNewApps.emit(true)
////                    }
//                    takeSnapshot(appsWithIcons)
//                }
//
//                // ✅ NEW: Trigger navigation to NewAppsScreen if new apps found
//                if (newApps.isNotEmpty()) {
//                    sendNotification()
//                    _navigateToNewApps.emit(true)
//                }

            } finally {
                _isLoading.value = false
            }
        }
    }


    private fun sendNotification() {
        val context = getApplication<Application>().applicationContext
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = buildNotification()
        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        notificationManager.notify(notificationId, notification)
    }

    // -----------------------------
    // ✅ NEW: Manually trigger snapshot (for testing)
    // -----------------------------
    fun forceSnapshot() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentApps = allAppsFromDb.first()
            takeSnapshot(currentApps)
        }
    }

    // -----------------------------
    // ✅ FOR TESTING: Force reset snapshot and clear new apps list
    // -----------------------------
    fun forceResetSnapshot() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentApps = allAppsFromDb.first()

            // Take snapshot of current apps
            val previousApps = currentApps.map { it.toPreviousApp() }
            db.previousAppDao().deleteAllApps()
            db.previousAppDao().insertAll(previousApps)

            // Mark snapshot taken
            AppApplication.sessionManager.markSnapshotTaken()

            // Clear new apps list
            _newlyInstalledApps.value = emptyList()
        }
    }

    // -----------------------------
    // ✅ FOR TESTING: Simulate new app detection
    // -----------------------------
    fun simulateNewAppInstall() {
        viewModelScope.launch(Dispatchers.IO) {
            // Get current apps
            val currentApps = allAppsFromDb.first()

            // Compare with snapshot
            val newApps = compareWithSnapshot(currentApps)
            _newlyInstalledApps.value = newApps

            // Send notifications
            newApps.forEach { app ->
//                sendNotification(app)
            }

            // ✅ Trigger navigation if new apps found
            if (newApps.isNotEmpty()) {
                _navigateToNewApps.emit(true)
            }
        }
    }

    // -----------------------------
    // ✅ Clear icon cache
    // -----------------------------
    fun clearIconCache() {
        iconCache.clear()
    }

    // -----------------------------
    // 🔟 Export filtered apps JSON
    // -----------------------------
    fun getFilteredAppsJson(): String {
        return Gson().toJson(filteredApps.value)
    }

    // -----------------------------
    // Notification setup
    // -----------------------------
    private fun createNotificationChannel() {
        val context = getApplication<Application>().applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "new_app_channel",
                "New App Installed",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when a new app is installed"
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    init {
     //   createNotificationChannel()
    }

    private fun buildNotification(): Notification {
        val context = getApplication<Application>().applicationContext

        // Create intent to open NewAppsScreen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "new_apps")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, "new_app_channel")
            .setContentTitle("New Apps Installed")
//            .setContentText("${app.riskBand.name} Risk • ${app.permissions.size} permissions")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

//    private fun sendNotification(app: AuditedApp) {
//        val context = getApplication<Application>().applicationContext
//        val notificationManager =
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        val notification = buildNotification(app)
//        notificationManager.notify(app.packageName.hashCode(), notification)
//    }

    // -----------------------------
    // ✅ Get snapshot information for UI
    // -----------------------------
    fun getSnapshotInfo(): String {
        return if (AppApplication.sessionManager.isFirstScanDone) {
            val lastDate = AppApplication.sessionManager.getLastSnapshotDate()
            val daysSince = AppApplication.sessionManager.getDaysSinceLastSnapshot()

            if (daysSince == 0) {
                "Last snapshot: Today"
            } else if (daysSince == 1) {
                "Last snapshot: Yesterday"
            } else {
                "Last snapshot: $lastDate ($daysSince days ago)"
            }
        } else {
            "No snapshot taken yet"
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearIconCache()
    }
}