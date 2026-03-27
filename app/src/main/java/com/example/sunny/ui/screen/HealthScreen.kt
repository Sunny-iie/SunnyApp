package com.example.sunny.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sunny.ui.theme.MorandiGreen
import com.example.sunny.ui.theme.MorandiBlue
import com.example.sunny.ui.theme.MorandiDark
import com.example.sunny.ui.theme.White
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import java.text.SimpleDateFormat
import androidx.compose.foundation.verticalScroll
import java.util.Date
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import com.example.sunny.data.HealthRecord
import com.example.sunny.data.ItemDao
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.example.sunny.showDatePicker
import com.example.sunny.ui.theme.MorandiBeige
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    records: List<HealthRecord>,
    itemDao: ItemDao,
    scope: CoroutineScope
) {
    var showSheet by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<HealthRecord?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "健康管理",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
            modifier = Modifier.padding(24.dp),
            color = MorandiDark
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(records) { record ->
                HealthRecordCard(record) {
                    selectedRecord = record
                    showSheet = true
                }
            }
        }

        // 健康模块的专用添加按钮
        Button(
            onClick = {
                selectedRecord = null
                showSheet = true
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MorandiBlue)
        ) {
            Text("新增记录")
        }
    }

    if (showSheet) {
        // 这里弹出专用的健康信息录入框（HealthFormSheet）
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                selectedRecord = null // 关键：关闭时清空状态，防止下次打开显示旧数据
            },
            sheetState = sheetState,
            containerColor = White,
            modifier = Modifier.imePadding() // 核心：防止软键盘弹出遮挡保存按钮
        ) {
            HealthFormSheet(
                initial = selectedRecord,
                onSave = { newRecord ->
                    // 使用传入的 scope 执行数据库异步操作
                    scope.launch {
                        if (selectedRecord == null) {
                            // 新增模式
                            itemDao.insertHealthRecord(newRecord)
                        } else {
                            // 编辑模式
                            itemDao.updateHealthRecord(newRecord)
                        }
                        showSheet = false // 保存成功后关闭
                        selectedRecord = null // 重置状态
                    }
                }
            )
        }
    }
}

@Composable
fun HealthRecordCard(record: HealthRecord, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // 类型图标
            val icon = when(record.type) {
                "体检" -> Icons.Filled.Assignment
                "病历" -> Icons.Filled.MedicalServices
                else -> Icons.Filled.Notes
            }

            Box(
                Modifier.size(40.dp).background(MorandiBlue.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MorandiBlue, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(record.title, style = MaterialTheme.typography.titleMedium, color = MorandiDark)
                Text(SimpleDateFormat("yyyy-MM-dd").format(Date(record.date)),
                    style = MaterialTheme.typography.labelSmall, color = MorandiBlue)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthFormSheet(
    initial: HealthRecord?,
    onSave: (HealthRecord) -> Unit
) {
    // 状态初始化：如果有 initial，则填充旧数据，否则为空
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "体检") }
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var medication by remember { mutableStateOf(initial?.medication ?: "") }
    var cost by remember { mutableStateOf(initial?.cost ?: "") }
    var doctor by remember { mutableStateOf(initial?.doctorName ?: "") }
    var date by remember { mutableLongStateOf(initial?.date ?: System.currentTimeMillis()) }

    val context = LocalContext.current
    val sdf = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()) // 确保内容多时可滑动
    ) {
        Text(
            text = if (initial == null) "✨ 新增健康记录" else "✏️ 修改健康记录",
            style = MaterialTheme.typography.headlineSmall,
            color = MorandiDark,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // 1. 标题输入
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("记录名称 (如：XX医院年度体检)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 类型选择
        Text("记录类型", style = MaterialTheme.typography.labelSmall, color = MorandiBlue)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("体检", "病历", "用药", "记录").forEach { t ->
                FilterChip(
                    selected = type == t,
                    onClick = { type = t },
                    label = { Text(t) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MorandiBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // 3. 日期选择卡片
        Card(
            onClick = { showDatePicker(context, date) { date = it } },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = CardDefaults.cardColors(containerColor = MorandiBeige.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("发生日期", style = MaterialTheme.typography.bodyMedium, color = MorandiDark)
                Text(sdf.format(Date(date)), color = MorandiBlue, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. 医生与费用 (并排展示)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = doctor,
                onValueChange = { doctor = it },
                label = { Text("主治医生") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("花费(元)") },
                modifier = Modifier.weight(0.8f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. 详情记录
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("详情/体检结论/医生嘱托") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 6. 保存按钮
        Button(
            onClick = {
                if (title.isNotBlank()) {
                    onSave(HealthRecord(
                        id = initial?.id ?: 0,
                        title = title,
                        type = type,
                        date = date,
                        content = content,
                        medication = medication,
                        cost = cost,
                        doctorName = doctor,
                        isDeleted = initial?.isDeleted ?: false
                    ))
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MorandiGreen)
        ) {
            Text("确认保存", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}