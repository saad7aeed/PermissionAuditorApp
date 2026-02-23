package com.example.permissionauditor.notification


import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.permissionauditor.db.AppDatabase
import com.example.permissionauditor.utlis.PackageScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppInstallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
            val packageName = intent.data?.schemeSpecificPart ?: return

            // Optional: ignore self
            if (packageName == context.packageName) return

            // Show notification
            showNotification(context, packageName)

            // Insert into Room DB (so Compose updates automatically)
            val app = PackageScanner(context).getAppByPackageName(packageName)
            if (app != null) {
                val db = AppDatabase.getInstance(context)
                CoroutineScope(Dispatchers.IO).launch {
                    db.auditedAppDao().insert(app)
                }
            }
        }
    }

    private fun showNotification(context: Context, packageName: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "new_app_channel"

        // Create channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "New App Installed",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when a new app is installed"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_menu_recent_history)
            .setContentTitle("New App Installed")
            .setContentText("Package: $packageName")
            .setAutoCancel(true)
            .build()

        notificationManager.notify(packageName.hashCode(), notification)
    }
}
