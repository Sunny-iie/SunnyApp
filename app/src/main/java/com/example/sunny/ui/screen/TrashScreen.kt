package com.example.sunny.ui.screen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sunny.data.Item
import com.example.sunny.ui.ItemCard
import com.example.sunny.ui.theme.MorandiBeige
import com.example.sunny.ui.theme.MorandiGreen
import com.example.sunny.ui.theme.MorandiBlue
import com.example.sunny.ui.theme.MorandiDark
import com.example.sunny.ui.theme.MorandiPink
import com.example.sunny.ui.theme.SunnyTheme
import com.example.sunny.ui.theme.White
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import java.text.SimpleDateFormat
import androidx.compose.foundation.verticalScroll
import java.util.Date
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale  // 必须是 java.util 路径下的
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.foundation.text.KeyboardOptions
import java.util.Calendar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import com.example.sunny.data.AppDatabase
import kotlinx.coroutines.launch
import com.example.sunny.ui.theme.MorandiRed
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.animation.Crossfade
import com.example.sunny.data.ItemDao
import kotlinx.coroutines.CoroutineScope
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import com.example.sunny.data.HealthRecord

@Composable
fun TrashScreen(
    items: List<Item>,               // 已删除的物品
    healthRecords: List<HealthRecord>, // 新增：已删除的健康记录
    itemDao: ItemDao,
    scope: CoroutineScope
) {
    // --- 控制对话框显示 ---
    var showDeleteDialog by remember { mutableStateOf(false) }
    // 存储点击删除时对应的具体操作（是删除物品还是健康记录）
    var pendingDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // 如果两类数据都为空，才显示空状态
    if (items.isEmpty() && healthRecords.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("回收站空空如也", color = MorandiBlue.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(Modifier.fillMaxSize()) {
            // --- 顶部大标题 ---
            item {
                Text(
                    "回收站",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    color = MorandiDark
                )
            }

            // --- 第一部分：物品清单回收 ---
            if (items.isNotEmpty()) {
                item { TrashHeader("已删除的物品") }
                items(items, key = { "trash_item_${it.id}" }) { item ->
                    TrashCard(
                        title = item.name,
                        subtitle = "原类别: ${item.category}",
                        onRestore = {
                            scope.launch { itemDao.updateItem(item.copy(isDeleted = false)) }
                        },
                        onDelete = {
                            // 关键：不直接删除，而是弹出对话框
                            pendingDeleteAction = { scope.launch { itemDao.deletePermanently(item) } }
                            showDeleteDialog = true
                        }
                    )
                }
            }

            // --- 第二部分：健康记录回收 ---
            if (healthRecords.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TrashHeader("已删除的健康记录")
                }
                items(healthRecords, key = { "trash_health_${it.id}" }) { record ->
                    TrashCard(
                        title = record.title,
                        subtitle = "就诊于: ${record.hospitalName.ifEmpty { "未记录医院" }}",
                        onRestore = {
                            scope.launch { itemDao.updateHealthRecord(record.copy(isDeleted = false)) }
                        },
                        onDelete = {
                            // 关键：弹出对话框
                            pendingDeleteAction = { scope.launch { itemDao.deleteHealthRecordPermanently(record) } }
                            showDeleteDialog = true
                        }
                    )
                }
            }

            // 底部留白
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
    // --- 优雅的莫兰迪风格确认对话框 ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteAction?.invoke()
                        showDeleteDialog = false
                    }
                ) {
                    Text("彻底删除", color = MorandiRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消", color = MorandiDark.copy(alpha = 0.6f))
                }
            },
            title = {
                Text("确认永久删除？", style = MaterialTheme.typography.titleLarge, color = MorandiDark)
            },
            text = {
                Text("此操作将从设备中永久抹除该记录，删除后将无法通过回收站恢复。", color = MorandiDark.copy(alpha = 0.8f))
            },
            containerColor = White,
            shape = RoundedCornerShape(28.dp), // 超大圆角显得更温和、高级
            tonalElevation = 6.dp
        )
    }
}

// --- 辅助小组件：分类标题 ---
@Composable
fun TrashHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MorandiBlue
    )
}

// --- 辅助小组件：通用的回收站卡片 ---
@Composable
fun TrashCard(
    title: String,
    subtitle: String,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MorandiDark)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MorandiBlue.copy(alpha = 0.7f))
            }

            // 恢复按钮
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.Restore, contentDescription = "恢复", tint = MorandiGreen)
            }

            // 彻底删除按钮
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteForever, contentDescription = "彻底删除", tint = MorandiRed.copy(alpha = 0.8f))
            }
        }
    }
}