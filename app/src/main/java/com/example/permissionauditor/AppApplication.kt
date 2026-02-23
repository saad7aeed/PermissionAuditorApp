package com.example.permissionauditor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.permissionauditor.db.AppDatabase
import com.example.permissionauditor.utlis.SessionManager

class AppApplication : Application() {
    private val TAG = AppApplication::class.simpleName

    companion object {
        lateinit var instance: Application
        lateinit var sessionManager: SessionManager
        lateinit var database: AppDatabase

    }

    override fun onCreate() {
        super.onCreate()
        initialize()
        createNotificationChannel()
    }


    private fun initialize() {
        instance = this
        sessionManager = SessionManager(applicationContext)
        database = AppDatabase.getInstance(applicationContext)

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "new_app_channel",
                "New App Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a new app is installed"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}