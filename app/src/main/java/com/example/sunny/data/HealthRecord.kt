package com.example.sunny.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,          // 标题（如：2024年度体检、感冒就诊）
    val type: String,           // 类型：体检、病历、用药、记录
    val date: Long,            // 日期
    val content: String = "",   // 具体详情（医生建议、体检结论）
    val medication: String = "", // 用药情况
    val cost: String = "",      // 费用记录
    val doctorName: String = "", // 医生姓名
    val isDeleted: Boolean = false
)