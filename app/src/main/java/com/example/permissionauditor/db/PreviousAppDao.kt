package com.example.permissionauditor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.permissionauditor.ui.model.PreviousApp
import kotlinx.coroutines.flow.Flow

@Dao
interface PreviousAppDao {
    @Query("SELECT * FROM previous_apps ORDER BY appLabel ASC")
    fun observeAll(): Flow<List<PreviousApp>>

    @Query("SELECT packageName FROM previous_apps")
    suspend fun getAllPackages(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<PreviousApp>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: PreviousApp)


    @Query("DELETE FROM previous_apps")
    suspend fun deleteAllApps()  // ✅ new function
}