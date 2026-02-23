package com.example.permissionauditor.ui.model.repo


import android.content.Context
import com.example.permissionauditor.db.AuditedAppDao
import com.example.permissionauditor.ui.model.AuditedApp
import com.example.permissionauditor.utlis.PackageScanner
import kotlinx.coroutines.flow.Flow

class AuditedAppRepository(
    private val context: Context,
    private val dao: AuditedAppDao
) {

    private val scanner = PackageScanner(context)

    // Observe apps from DB
    fun observeApps(): Flow<List<AuditedApp>> = dao.observeAll()

    // Initial scan to populate DB
    suspend fun initialScan() {
        val apps = scanner.getAllApps(includeSystemApps = false)
        dao.insertAll(apps)
    }

    // Scan for new apps, return new installs
    suspend fun scanForNewApps(): List<AuditedApp> {
        val stored = dao.getAllPackages().toSet()
        val current = scanner.getAllApps(includeSystemApps = false)

        val newApps = current.filter { it.packageName !in stored }

        dao.insertAll(newApps)
        return newApps
    }
}
