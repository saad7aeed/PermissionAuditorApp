package com.example.permissionauditor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.permissionauditor.ui.model.AuditedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditedAppDao {

    @Query("SELECT * FROM audited_apps ORDER BY appLabel ASC")
    fun observeAll(): Flow<List<AuditedApp>>

    @Query("SELECT packageName FROM audited_apps")
    suspend fun getAllPackages(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<AuditedApp>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: AuditedApp)


    @Query("DELETE FROM audited_apps")
    suspend fun deleteAllApps()  // ✅ new function
}
