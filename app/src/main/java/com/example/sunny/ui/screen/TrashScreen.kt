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
@Composable
fun TrashScreen(
    items: List<Item>,
    itemDao: ItemDao,
    scope: CoroutineScope
) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("回收站空空如也", color = MorandiBlue)
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(items, key = { "trash_${it.id}" }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium, color = MorandiDark)
                            Text("原类别: ${item.category}", style = MaterialTheme.typography.labelSmall, color = MorandiBlue)
                        }

                        // 1. 恢复按钮
                        IconButton(onClick = {
                            scope.launch { itemDao.updateItem(item.copy(isDeleted = false)) }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "恢复", tint = MorandiGreen)
                        }

                        // 2. 彻底删除按钮
                        IconButton(onClick = {
                            scope.launch { itemDao.deletePermanently(item) }
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "彻底删除", tint = MorandiRed)
                        }
                    }
                }
            }
        }
    }
}