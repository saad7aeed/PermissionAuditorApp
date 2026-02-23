package com.example.permissionauditor.db


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.permissionauditor.ui.model.AuditedApp
import com.example.permissionauditor.ui.model.PreviousApp
import com.example.permissionauditor.ui.model.converter.AppTypeConverters

@Database(
    entities = [AuditedApp::class,
        PreviousApp::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun auditedAppDao(): AuditedAppDao
    abstract fun previousAppDao(): PreviousAppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "permission_auditor.db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
