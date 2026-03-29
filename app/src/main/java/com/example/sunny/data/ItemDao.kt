package com.example.sunny.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    // 逻辑：首先按“是否已过期”降序排（过期的为1，未过期的为0，所以1在前）
    // 其次按“过期时间”升序排（越快过期的越靠前）
    // 1. 获取正常物品（未删除的）
    @Query("SELECT * FROM items WHERE isDeleted = 0 ORDER BY (expiryDate < :currentTime) DESC, expiryDate ASC")
    fun getActiveItems(currentTime: Long): Flow<List<Item>>

    // 2. 获取回收站物品
    @Query("SELECT * FROM items WHERE isDeleted = 1 ORDER BY expiryDate ASC")
    fun getDeletedItems(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)
    @Delete
    suspend fun deletePermanently(item: Item)

    // 健康管理
    @Query("SELECT * FROM health_records WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllHealthRecords(): Flow<List<HealthRecord>>

    // --- 健康记录：正常显示 ---
    @Query("SELECT * FROM health_records WHERE isDeleted = 0 ORDER BY date DESC")
    fun getActiveHealthRecords(): Flow<List<HealthRecord>>

    // --- 健康记录：回收站显示 ---
    @Query("SELECT * FROM health_records WHERE isDeleted = 1 ORDER BY date DESC")
    fun getDeletedHealthRecords(): Flow<List<HealthRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: HealthRecord)

    @Update
    suspend fun updateHealthRecord(record: HealthRecord)

    // --- 彻底删除 ---
    @Delete
    suspend fun deleteHealthRecordPermanently(record: HealthRecord)
}