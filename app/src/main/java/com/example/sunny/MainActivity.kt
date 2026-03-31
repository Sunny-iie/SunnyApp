package com.example.sunny

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
import androidx.compose.material.icons.filled.HealthAndSafety
import com.example.sunny.ui.theme.FoodBg
import com.example.sunny.ui.theme.ExpiredBg
import com.example.sunny.ui.screen.MainListScreen
import com.example.sunny.ui.screen.TrashScreen
import com.example.sunny.ui.screen.HealthScreen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 初始化数据库
        val db = AppDatabase.getDatabase(applicationContext)
        val itemDao = db.itemDao()

        setContent {
            SunnyTheme {
                // --- 页面状态管理 ---
                var currentTab by remember { mutableIntStateOf(0) }
                val scope = rememberCoroutineScope()

                // --- 数据流监听 ---
                // 获取未删除的物品（主列表）
                val activeItems by itemDao.getActiveItems(System.currentTimeMillis()).collectAsState(initial = emptyList())
                // 获取已标记删除的物品（回收站）
                val deletedItems by itemDao.getDeletedItems().collectAsState(initial = emptyList())
                val healthRecords by itemDao.getAllHealthRecords().collectAsState(initial = emptyList())
                val deletedHealth by itemDao.getDeletedHealthRecords().collectAsState(initial = emptyList()) // 确保 DAO 有这个方法

                // --- 弹窗与编辑状态 ---
                var showSheet by remember { mutableStateOf(false) }
                var selectedItem by remember { mutableStateOf<Item?>(null) }
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MorandiBeige, // 设置全局背景色
                    bottomBar = {
                        NavigationBar(containerColor = White, tonalElevation = 8.dp) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.List, contentDescription = "清单") },
                                label = { Text("清单") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MorandiGreen,
                                    indicatorColor = FoodBg
                                )
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Filled.HealthAndSafety, "健康") }, // 需导入相应图标
                                label = { Text("健康") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = MorandiBlue)
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.Delete, contentDescription = "回收站") },
                                label = { Text("回收站") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MorandiRed,
                                    indicatorColor = ExpiredBg
                                )
                            )
                        }
                    },
                    floatingActionButton = {
                        if (currentTab == 0) { // 仅在主页显示添加按钮
                            FloatingActionButton(
                                onClick = {
                                    selectedItem = null // 清空选择，代表新增
                                    showSheet = true
                                },
                                containerColor = MorandiGreen
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "添加", tint = White)
                            }
                        }
                    }
                ) { innerPadding ->
                    // 使用 Crossfade 实现丝滑的页面切换动画
                    Crossfade(
                        targetState = currentTab,
                        modifier = Modifier.padding(innerPadding)
                    ) { tab ->
                        when (tab) {
                            0 -> MainListScreen(
                                itemsList = activeItems,
                                itemDao = itemDao,
                                scope = scope,
                                onEditItem = { item ->
                                    selectedItem = item // 记录点击的条目
                                    showSheet = true    // 打开编辑弹窗
                                }
                            )
                            1 -> HealthScreen(healthRecords, itemDao, scope) // 健康页面
                            2 -> TrashScreen(deletedItems, deletedHealth, itemDao, scope)
                        }
                    }

                    // --- 统一的底部弹窗管理 ---
                    if (showSheet) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                showSheet = false
                                selectedItem = null
                            },
                            sheetState = sheetState,
                            containerColor = White,
                            modifier = Modifier.imePadding() // 避让键盘
                        ) {
                            ItemFormSheet(
                                initialItem = selectedItem,
                                onSave = { updatedItem ->
                                    scope.launch {
                                        if (selectedItem == null) {
                                            itemDao.insertItem(updatedItem) // 执行插入
                                        } else {
                                            itemDao.updateItem(updatedItem) // 执行更新
                                        }
                                    }
                                    showSheet = false
                                    selectedItem = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormSheet(
    initialItem: Item? = null,
    onSave: (Item) -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var memo by remember { mutableStateOf(initialItem?.memo ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "食物") }
    var prodDate by remember { mutableLongStateOf(initialItem?.productionDate ?: System.currentTimeMillis()) }
    var expDate by remember { mutableLongStateOf(initialItem?.expiryDate ?: (System.currentTimeMillis() + 86400000L * 30)) }

    val categories = listOf("食物", "药品", "护肤品", "化妆品")
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var shelfLifeMonths by remember { mutableStateOf("") } // 新增：保质期（月）输入状态

    // 自动计算逻辑：当生产日期或保质期月份变化时，更新有效期
    fun calculateExpDate(monthsStr: String, pDate: Long) {
        val months = monthsStr.toIntOrNull() ?: return
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = pDate
        calendar.add(Calendar.MONTH, months) // 按照自然月增加，比按天算更准
        // 更新 expDate 状态（注意：需要确保 expDate 是 remember 状态）
    }

    // 关键修复：在 Column 中使用 weight(1f, fill = false) 避免测量无限高度
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = if (initialItem == null) "✨ 录入新物品" else "✏️ 编辑信息",
            style = MaterialTheme.typography.headlineSmall,
            color = MorandiDark,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 20) name = it },
            label = { Text("物品名称 (${name.length}/20)", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray,) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("类型", style = MaterialTheme.typography.bodySmall, color = MorandiBlue)
        Spacer(modifier = Modifier.height(2.dp))   // 减小间距
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MorandiGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        // --- 核心优化：日期与保质期三连框 ---
        Text("日期详情 (点击修改日期或输入月数)", style = MaterialTheme.typography.labelMedium, color = MorandiBlue)
        Spacer(modifier = Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 1. 生产日期卡片
            DateInfoCard(
                label = "生产日期",
                value = sdf.format(Date(prodDate)),
                color = MorandiBlue,
                modifier = Modifier.weight(1.1f),
                onClick = {
                    showDatePicker(context, prodDate) {
                        prodDate = it
                        // 如果有月数，联动更新有效期
                        val months = shelfLifeMonths.toIntOrNull() ?: 0
                        if (months > 0) {
                            val cal = Calendar.getInstance().apply { timeInMillis = it }
                            cal.add(Calendar.MONTH, months)
                            expDate = cal.timeInMillis
                        }
                    }
                }
            )

            // 2. 保质期输入卡片 (自定义样式，替代原生的 TextField 以节省空间)
            Surface(
                modifier = Modifier.weight(0.8f).height(60.dp),
                color = MorandiBeige.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MorandiBlue.copy(alpha = 0.1f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("保质期", style = MaterialTheme.typography.labelSmall, color = MorandiBlue)
                    BasicTextField(
                        value = shelfLifeMonths,
                        onValueChange = {
                            if (it.length <= 3) {
                                shelfLifeMonths = it
                                val months = it.toIntOrNull() ?: 0
                                if (months > 0) {
                                    val cal = Calendar.getInstance().apply { timeInMillis = prodDate }
                                    cal.add(Calendar.MONTH, months)
                                    expDate = cal.timeInMillis
                                }
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            color = MorandiDark
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(40.dp),
                        decorationBox = { innerTextField ->
                            if (shelfLifeMonths.isEmpty()) Text("月", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray, textAlign = TextAlign.Center)
                            innerTextField()
                        }
                    )
                }
            }

            // 3. 有效期卡片
            DateInfoCard(
                label = "有效期至",
                value = sdf.format(Date(expDate)),
                color = MorandiRed,
                modifier = Modifier.weight(1.1f),
                onClick = {
                    showDatePicker(context, expDate) {
                        expDate = it
                        shelfLifeMonths = "" // 手动选日期后清空月数输入
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 备注信息
        OutlinedTextField(
            value = memo,
            onValueChange = { memo = it },
            label = { Text("备注/备忘信息", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray,) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MorandiGreen,
                unfocusedBorderColor = MorandiBlue.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        Item(
                            id = initialItem?.id ?: 0,
                            name = name,
                            category = category,
                            memo = memo,
                            productionDate = prodDate,
                            expiryDate = expDate
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MorandiGreen)
        ) {
            Text("保存记录", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        // 底部留白，确保键盘弹出时能滑到底
        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun showDatePicker(context: android.content.Context, initialDate: Long, onDateSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialDate }
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            onDateSelected(selectedCalendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

// 辅助组件：抽取出来的日期展示卡片，视觉更统一且不换行
@Composable
fun DateInfoCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
            Text(value, style = MaterialTheme.typography.bodySmall, color = MorandiDark, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteWrapper(
    item: Item,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false, // 仅允许左滑
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                MorandiRed.copy(alpha = 0.8f) // 使用你定义的枯玫瑰红
            } else Color.Transparent

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp) // 与 ItemCard 外边距一致
                    .clip(RoundedCornerShape(16.dp)) // 与 ItemCard 圆角一致
                    .background(color),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
    ) {
        // 条目内容
        ItemCard(item = item, onClick = onClick)
    }
}