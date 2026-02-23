package com.example.permissionauditor.utlis

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

class SessionManager(
    // Context
    var _context: Context,
) {
    var pref: SharedPreferences

    // Editor for Shared preferences
    var editor: SharedPreferences.Editor

    // Shared pref mode
    var PRIVATE_MODE = 0

    /**
     * Create privacy mode setting
     */
    fun createPrivacy(
        flag: Boolean,
    ) {
        editor.putBoolean(KEY_MODE, flag)
        editor.commit()
    }

    val getPrivacy: Boolean
        get() {
            return pref.getBoolean(KEY_MODE, false)
        }

    /**
     * Create system application setting
     */
    fun createIncludeSystemApplication(
        flag: Boolean,
    ) {
        editor.putBoolean(KEY_SYSTEM, flag)
        editor.commit()
    }

    val getSystemApplication: Boolean
        get() {
            return pref.getBoolean(KEY_SYSTEM, false)
        }

    // -----------------------------
    // ✅ NEW: Snapshot Management
    // -----------------------------

    /**
     * Store the timestamp of last snapshot
     */
    var lastSnapshotTime: Long
        get() = pref.getLong(KEY_LAST_SNAPSHOT, 0L)
        set(value) {
            editor.putLong(KEY_LAST_SNAPSHOT, value)
            editor.commit()
        }

    /**
     * Check if first scan has been completed
     */
    var isFirstScanDone: Boolean
        get() = pref.getBoolean(KEY_FIRST_SCAN_DONE, false)
        set(value) {
            editor.putBoolean(KEY_FIRST_SCAN_DONE, value)
            editor.commit()
        }

    /**
     * Check if a new snapshot should be taken
     * Returns true if:
     * 1. First scan not done yet, OR
     * 2. It's Monday AND at least 7 days have passed since last snapshot
     */
    fun shouldTakeSnapshot(): Boolean {
        // If first scan not done, take snapshot
        if (!isFirstScanDone) return true

        val lastSnapshot = lastSnapshotTime
        if (lastSnapshot == 0L) return true

        val lastSnapshotCal = Calendar.getInstance().apply {
            timeInMillis = lastSnapshot
        }
        val currentCal = Calendar.getInstance()

        // Check if it's been at least 7 days since last snapshot
        val daysSinceSnapshot =
            ((currentCal.timeInMillis - lastSnapshot) / (1000 * 60 * 60 * 24)).toInt()

        if (daysSinceSnapshot < 7) return false

        // Check if current day is Monday (Calendar.MONDAY = 2)
        return currentCal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
    }

    /**
     * Mark that a snapshot has been taken
     */
    fun markSnapshotTaken() {
        lastSnapshotTime = System.currentTimeMillis()
        if (!isFirstScanDone) {
            isFirstScanDone = true
        }
    }

    /**
     * Reset snapshot tracking (useful for testing)
     */
    fun resetSnapshotTracking() {
        editor.remove(KEY_LAST_SNAPSHOT)
        editor.remove(KEY_FIRST_SCAN_DONE)
        editor.commit()
    }

    /**
     * Get days since last snapshot
     */
    fun getDaysSinceLastSnapshot(): Int {
        val lastSnapshot = lastSnapshotTime
        if (lastSnapshot == 0L) return -1

        val currentTime = System.currentTimeMillis()
        return ((currentTime - lastSnapshot) / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * Get formatted date of last snapshot
     */
    fun getLastSnapshotDate(): String {
        val lastSnapshot = lastSnapshotTime
        if (lastSnapshot == 0L) return "Never"

        val calendar = Calendar.getInstance().apply { timeInMillis = lastSnapshot }
        return "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
    }

    companion object {
        // Shared pref file name
        private const val PREF_NAME = "SPF_PREF"

        // Existing keys
        const val KEY_MODE = "privacy_mode"
        const val KEY_SYSTEM = "system_mode"

        // New snapshot keys
        const val KEY_LAST_SNAPSHOT = "last_snapshot_time"
        const val KEY_FIRST_SCAN_DONE = "first_scan_done"
    }

    // Constructor
    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
        editor.apply()
    }
}