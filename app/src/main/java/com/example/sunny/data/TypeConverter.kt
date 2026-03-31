package com.example.sunny.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// --- 增加转换器，让 Room 认识 List<String> ---
class Converters {
    private val gson = Gson()
    @TypeConverter
    fun fromString(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return Gson().toJson(list ?: emptyList<String>())
    }
    // --- List<HealthMetric> 转换（用于体检指标表格） ---
    @TypeConverter
    fun fromMetricList(value: String?): List<HealthMetric> {
        val listType = object : TypeToken<List<HealthMetric>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun toMetricList(list: List<HealthMetric>?): String {
        return gson.toJson(list ?: emptyList<HealthMetric>())
    }
    // 在 Converters 类中添加
    @TypeConverter
    fun fromExamReportList(value: String?): List<HealthExamReport> {
        val listType = object : TypeToken<List<HealthExamReport>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun toExamReportList(list: List<HealthExamReport>?): String {
        return Gson().toJson(list ?: emptyList<HealthExamReport>())
    }
}