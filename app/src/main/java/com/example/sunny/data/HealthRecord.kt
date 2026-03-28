package com.example.sunny.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String,           // "体检" 或 "病历"
    val diseaseName: String = "常规",
    val hospitalName: String = "",
    val date: Long,
    val content: String = "",
    val medication: String = "",
    val cost: String = "",
    val imageUris: List<String> = emptyList(), // 确保 Converters 已配置
    val isDeleted: Boolean = false,
    val doctorName: String = "",
)