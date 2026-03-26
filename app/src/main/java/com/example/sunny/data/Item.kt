package com.example.sunny.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String = "食物",             // 设置默认值为 "食物"
    val memo: String = "",                   // 设置默认值为空字符串
    val productionDate: Long = System.currentTimeMillis(), // 默认为当前时间
    val expiryDate: Long,                    // 过期时间通常必须手动填，所以不设默认值
    val shelfLifeDays: Int = 0,
    val isDeleted: Boolean = false // 新增：标记是否已删除，默认为 false
)