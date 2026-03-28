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
import androidx.compose.foundation.clickable
import com.example.sunny.ItemFormSheet
import com.example.sunny.SwipeToDeleteWrapper
import com.example.sunny.data.ItemDao
import kotlinx.coroutines.CoroutineScope
import kotlin.compareTo
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.sunny.ui.theme.MorandiMauve



@Composable
fun MainListScreen(
    itemsList: List<Item>,
    itemDao: ItemDao,
    scope: CoroutineScope,
    onEditItem: (Item) -> Unit // 当点击条目想编辑时，通知父组件
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val categories = listOf("食物", "药品", "护肤品", "化妆品")
    val currentTime = System.currentTimeMillis()

    // 2. 过滤逻辑
    val filteredItems = if (selectedCategory == null) itemsList else itemsList.filter { it.category == selectedCategory }

    // 逻辑分层：过滤出已过期和未过期的
    val expiredItems = filteredItems.filter { it.expiryDate < currentTime }
    val activeItems = filteredItems.filter { it.expiryDate >= currentTime }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- 核心优化：标题 + 右侧 2x2 精致按钮组 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start  = 24.dp, top = 32.dp, bottom =16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧大方标题
            Column(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { selectedCategory = null }
            ) {
                Text(
                    text = "物品清单",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold, // 稍微加粗一点，显得更大方
                        letterSpacing = 1.sp
                    ),
                    color = MorandiDark
                )
                // 增加一个微小的筛选状态提示
                Text(
                    text = if (selectedCategory == null) "全部物品" else "筛选: $selectedCategory",
                    style = MaterialTheme.typography.labelSmall,
                    color = MorandiBlue.copy(alpha = 0.6f)
                )
            }
            // 右侧 2x2 矩阵
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // 【功能】：点击传入分类名称
                    MiniCategoryChip("食物", MorandiGreen, selectedCategory) { selectedCategory = it }
                    MiniCategoryChip("药品", MorandiBlue, selectedCategory) { selectedCategory = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniCategoryChip("护肤品", MorandiMauve, selectedCategory) { selectedCategory = it }
                    MiniCategoryChip("化妆品", MorandiMauve, selectedCategory) { selectedCategory = it }
                }
            }
        }

        // --- 列表区域 ---
        if (filteredItems.isEmpty()) {
            // 【优化】：当该分类下没东西时的提示
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (selectedCategory == null) "🌿万物有期，等君录入" else "🌿该分类下暂无物品",
                    color = MorandiBlue.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // 已过期区域（使用过滤后的 expiredItems）
                if (expiredItems.isNotEmpty()) {
                    item(key = "header_expired_${selectedCategory ?: "all"}") {
                        Text("⚠️ 需处理", modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.labelSmall, color = MorandiRed.copy(alpha = 0.7f))
                    }
                    items(expiredItems, key = { "exp_${it.id}" }) { item ->
                        SwipeToDeleteWrapper(
                            item = item,
                            onDelete = { scope.launch { itemDao.updateItem(item.copy(isDeleted = true)) } },
                            onClick = { onEditItem(item) }
                        )
                    }
                }

                // 存储中区域（使用过滤后的 activeItems）
                if (activeItems.isNotEmpty()) {
                    item(key = "header_active_${selectedCategory ?: "all"}") {
                        Text("✅ 存储中", modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.labelSmall, color = MorandiGreen.copy(alpha = 0.7f))
                    }
                    items(activeItems, key = { "active_${it.id}" }) { item ->
                        SwipeToDeleteWrapper(
                            item = item,
                            onDelete = { scope.launch { itemDao.updateItem(item.copy(isDeleted = true)) } },
                            onClick = { onEditItem(item) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun MiniCategoryChip(
    label: String,
    themeColor: Color,
    selectedCategory: String?,
    onSelect: (String) -> Unit
) {
    val isSelected = selectedCategory == label

    Surface(
        onClick = { onSelect(label) },
        // 稍微加宽尺寸（60x26），让视觉更舒展
        modifier = Modifier.size(width = 62.dp, height = 28.dp),
        // 选中时使用淡色，未选中时使用接近背景的极浅色
        color = if (isSelected) themeColor.copy(alpha = 0.25f) else MorandiBlue.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp), // 圆角稍微调大，更顺滑
        border = if (isSelected) BorderStroke(1.dp, themeColor.copy(alpha = 0.4f)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                // 字体略微调大到 11sp，兼顾精致与易读
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) themeColor else MorandiDark.copy(alpha = 0.6f)
            )
        }
    }
}