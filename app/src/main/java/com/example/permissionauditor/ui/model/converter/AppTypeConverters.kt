package com.example.permissionauditor.ui.model.converter

import androidx.room.TypeConverter
import com.example.permissionauditor.ui.model.AppPermission
import com.example.permissionauditor.ui.model.RiskBand
import com.example.permissionauditor.ui.model.SpecialCapability

class AppTypeConverters {

    // ✅ Existing - List<String>
    @TypeConverter
    fun fromStringList(value: List<String>): String =
        value.joinToString(",")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(",")

    // ✅ Existing - List<SpecialCapability>
    @TypeConverter
    fun fromCapabilities(value: List<SpecialCapability>): String =
        value.joinToString(",") { it.name }

    @TypeConverter
    fun toCapabilities(value: String): List<SpecialCapability> =
        if (value.isEmpty()) emptyList()
        else value.split(",").map { SpecialCapability.valueOf(it) }

    // ✅ Existing - RiskBand
    @TypeConverter
    fun fromRiskBand(band: RiskBand): String = band.name

    @TypeConverter
    fun toRiskBand(value: String): RiskBand =
        RiskBand.valueOf(value)

    // ✅ NEW - List<AppPermission>
    // Format: "permission.name:true|android.permission.CAMERA:false|..."
    @TypeConverter
    fun fromAppPermissionList(value: List<AppPermission>): String =
        value.joinToString("|") { "${it.name}:${it.isGranted}" }

    @TypeConverter
    fun toAppPermissionList(value: String): List<AppPermission> =
        if (value.isEmpty()) emptyList()
        else value.split("|").map { entry ->
            val lastColon = entry.lastIndexOf(":")
            val name = entry.substring(0, lastColon)
            val isGranted = entry.substring(lastColon + 1).toBoolean()
            AppPermission(name = name, isGranted = isGranted)
        }
}