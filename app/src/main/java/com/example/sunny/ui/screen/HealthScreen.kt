package com.example.sunny.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.horizontalScroll
import com.example.sunny.data.HealthRecord
import com.example.sunny.data.ItemDao
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.example.sunny.showDatePicker
import com.example.sunny.ui.theme.MorandiBeige
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sunny.data.HealthMetric
import com.example.sunny.data.HealthTemplates
import com.example.sunny.ui.theme.MorandiRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun HealthScreen(
    records: List<HealthRecord>,
    itemDao: ItemDao,
    scope: CoroutineScope
) {
    var showSheet by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<HealthRecord?>(null) }

    // 按疾病名称分组逻辑
    val groupedRecords = records.groupBy { it.diseaseName }
    var isEditing by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true) // 关键：全屏展示

    // 使用 Scaffold 管理布局，让新增按钮悬浮，更精致
    Scaffold(
        containerColor = MorandiBeige,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedRecord = null
                    showSheet = true
                },
                containerColor = MorandiBlue, // 健康模块使用蓝色调
                contentColor = White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "新增记录")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "健康档案",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold, // 稍微加粗一点，显得更大方
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                color = MorandiDark
            )

            if (records.isEmpty()) {
                // 空状态提示
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("记录点滴健康，守护美好生活", color = MorandiBlue.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedRecords.forEach { (disease, recordList) ->
                        // 1. 分组标题（吸顶效果）
                        stickyHeader {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MorandiBeige.copy(alpha = 0.95f) // 磨砂透明感
                            ) {
                                Text(
                                    text = "📁 $disease 相关记录",
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MorandiBlue
                                )
                            }
                        }

                        // 2. 该分组下的所有记录卡片
                        items(recordList, key = { "health_${it.id}" }) { record ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        scope.launch {
                                            // 标记为已删除（进入回收站）
                                            itemDao.updateHealthRecord(record.copy(isDeleted = true))
                                        }
                                        true
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                        MorandiRed.copy(alpha = 0.8f) else Color.Transparent
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(color),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.padding(end = 20.dp))
                                    }
                                }
                            ) {
                                HealthRecordCard(record) {
                                    selectedRecord = record
                                    showSheet = true
                                }
                            }
                        }
                    }

                    // 3. 底部留白，防止内容被悬浮按钮遮挡
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // --- 录入/编辑弹窗 ---
    if (showSheet) {

        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                selectedRecord = null
                isEditing = false // 重置状态
            },
            sheetState = sheetState,
            containerColor = White,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxWidth().heightIn(min =0.dp, max = 880.dp) // 占据屏幕90%高度，保证空间足够

        ) {
            // 关键：在内容容器里处理底部导航栏的边距
            Box(modifier = Modifier.navigationBarsPadding()) {
                if (selectedRecord != null && !isEditing) {
                    // 模式 A：查看详情
                    HealthDetailView(
                        record = selectedRecord!!,
                        onEditClick = { isEditing = true } // 点击编辑切换到 Form
                    )
                } else {
                    // 模式 B：编辑或新增
                    HealthFormSheet(
                        initial = selectedRecord,
                        onSave = { newRecord ->
                            scope.launch {
                                if (selectedRecord == null) itemDao.insertHealthRecord(newRecord)
                                else itemDao.updateHealthRecord(newRecord)
                                showSheet = false
                                selectedRecord = null
                                isEditing = false
                            }
                        }
                    )
                }
            }
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
                    style = MaterialTheme.   typography.labelSmall, color = MorandiBlue)
                // 展示医院和附件数量
                Row {
                    if (record.hospitalName.isNotEmpty()) {
                        Text("📍${record.hospitalName}  ", style = MaterialTheme.typography.labelSmall)
                    }
                    if (record.imageUris.isNotEmpty()) {
                        Text("🖼️ ${record.imageUris.size}个附件", style = MaterialTheme.typography.labelSmall)
                    }
                }
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

    var diseaseName by remember { mutableStateOf(initial?.diseaseName ?: "常规") }
    var hospitalName by remember { mutableStateOf(initial?.hospitalName ?: "") }
    var imageUris by remember { mutableStateOf(initial?.imageUris ?: emptyList()) }
    var metrics by remember { mutableStateOf(initial?.metrics ?: emptyList<HealthMetric>()) }
    var isScanning by remember { mutableStateOf(false) }

    // 1. 修改选择器，支持所有图片和 PDF
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            // 关键：申请永久保留该文件的访问权限
            try {
                // 关键：必须获取永久读取权限，否则后台渲染 PDF 会因为权限不足报错
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 将新选择的 Uri 合并到列表
        imageUris = imageUris + uris.map { it.toString() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // 避开底部导航条
            .imePadding()            // 关键：避开软键盘，防止键盘挡住输入框
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
            label = { Text("记录名称 (如：XX医院年度体检)", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray,) },
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

        Spacer(modifier = Modifier.height(8.dp))

        // 4. 医生与费用 (并排展示)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = doctor,
                onValueChange = { doctor = it },
                label = { Text("主治医生", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray,) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("花费(元)", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray,) },
                modifier = Modifier.weight(0.8f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 疾病分组与医院输入
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = diseaseName,
                onValueChange = { diseaseName = it },
                label = { Text("分组 (如:消化系统)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = hospitalName,
                onValueChange = { hospitalName = it },
                label = { Text("就诊医院", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray,) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 图片展示与上传区
        Text("报告及照片附件", style = MaterialTheme.typography.labelSmall, color = MorandiBlue)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 修改原本的 IconButton 或者是上传按钮的 onClick
            Button(
                onClick = { launcher.launch(arrayOf("image/*", "application/pdf")) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MorandiBlue.copy(alpha = 0.8f))
            ) {
                Icon(Icons.Default.AttachFile, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("添加照片或PDF报告", style = MaterialTheme.typography.labelMedium)
            }

            // 显示已选图片（建议使用 Coil 库显示预览图）
            imageUris.forEach { uri ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MorandiBeige)
                ) {
                    val isPdf = uri.lowercase().contains(".pdf") || uri.contains("pdf")

                    if (isPdf) {
                        // PDF 预览图
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, tint = MorandiRed, modifier = Modifier.size(28.dp))
                            Text("PDF", fontSize = 10.sp, color = MorandiRed)
                        }
                    } else {
                        // 图片预览图 (使用 AsyncImage)
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop // 裁剪填充
                        )
                    }

                    // 可选：增加一个删除小叉号，方便取消选错的图
                    IconButton(
                        onClick = { imageUris = imageUris.filter { it != uri } },
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

        }
        Spacer(modifier = Modifier.height(24.dp))

        if (type == "体检") {
            Spacer(modifier = Modifier.height(16.dp))

            Text("体检指标", style = MaterialTheme.typography.labelSmall, color = MorandiBlue)

            // 如果当前列表为空，显示“添加”大按钮
            if (metrics.isEmpty()) {
                OutlinedButton(
                    onClick = { metrics = HealthTemplates.defaultMetrics }, // 👈 一键导入刚才定义的模板
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddChart, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("添加常用体检指标")
                }
            }
            if (imageUris.isNotEmpty()) {
//                Spacer(modifier = Modifier.height(2.dp))
                Button(
                    onClick = {
                        isScanning = true
                        // 尝试识别第一张图
                        val uri = Uri.parse(imageUris.first())
                        com.example.sunny.util.MetricScanner.scanImage(context, uri) { results ->
                            if (results.isEmpty()) {
                                Toast.makeText(context, "未能识别到有效指标，请确保字迹清晰", Toast.LENGTH_LONG).show()
                            } else {
                                // 1. 确定底表（如果没有点击过模板，就用默认模板当底）
                                val baseList = if (metrics.isEmpty()) HealthTemplates.defaultMetrics else metrics

                                // 2. 填充数据
                                val updatedList = baseList.map { item ->
                                    if (results.containsKey(item.label)) {
                                        // 如果识别到了，用识别的值
                                        item.copy(value = results[item.label] ?: "")
                                    } else {
                                        item
                                    }
                                }

                                // 3. 强制更新 Compose 状态
                                metrics = updatedList

                                isScanning = false
                                Toast.makeText(context, "成功识别 ${results.size} 个项目", Toast.LENGTH_SHORT).show()
                            }
                            isScanning = false // 确保无论如何都停止加载圈
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MorandiBlue),
                    enabled = !isScanning,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, null)
                        Spacer(Modifier.width(4.dp))
                        Text("识别首张附件并填表")
                    }
                }
            }

            // 渲染指标录入区域
            metrics.forEachIndexed { index, metric ->
                MetricEditRow(
                    metric = metric,
                    onUpdate = { updated ->
                        // 更新列表中对应的项
                        val newList = metrics.toMutableList()
                        newList[index] = updated
                        metrics = newList
                    },
                    onDelete = {
                        val newList = metrics.toMutableList()
                        newList.removeAt(index)
                        metrics = newList
                    }
                )
            }

            // 允许在模板之外手动新增自定义行
            if (metrics.isNotEmpty()) {
                TextButton(onClick = { metrics = metrics + HealthMetric("", "", "") }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Text("新增自定义指标", fontSize = 12.sp)
                }
            }
        }


        // 5. 详情记录
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("详情/体检结论/医生嘱托", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray,) },
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
                        isDeleted = initial?.isDeleted ?: false,
                        diseaseName = diseaseName,
                        hospitalName = hospitalName,
                        imageUris = imageUris,
                        metrics = metrics
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

@Composable
@ExperimentalLayoutApi
fun HealthDetailView(record: HealthRecord, onEditClick: () -> Unit) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()) // 确保可以滑动
    ) {
        // --- 顶部标题栏 ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.title, style = MaterialTheme.typography.headlineSmall, color = MorandiDark)
                Text("记录类型：${record.type}", style = MaterialTheme.typography.labelMedium, color = MorandiBlue)
            }
            // 编辑按钮
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.background(MorandiBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Edit, "编辑", tint = MorandiBlue, modifier = Modifier.size(20.dp))
            }
        }

        // --- 核心信息卡片 (蓝色背景区) ---
        Surface(
            color = MorandiBlue.copy(alpha = 0.05f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow("就诊时间", sdf.format(Date(record.date)))

                // 重点修复：显示医院
                InfoRow("就诊医院", record.hospitalName.ifEmpty { "未记录" })

                InfoRow("主治医生", record.doctorName.ifEmpty { "未记录" })
                InfoRow("产生费用", if(record.cost.isEmpty()) "¥ 0" else "¥ ${record.cost}")
            }
        }

        if (record.metrics.any { it.value.isNotEmpty() }) {
            Text(
                "检查项目明细",
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MorandiBlue
            )
            // 调用我们下面定义的表格组件
            HealthReportTable(record.metrics)
        }

        // --- 详细描述 ---
        if (record.content.isNotEmpty()) {
            Text("详情描述与建议", modifier = Modifier.padding(top = 24.dp), style = MaterialTheme.typography.titleSmall, color = MorandiBlue)
            Text(record.content, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = MorandiDark, lineHeight = 24.sp)
        }

        // --- 附件展示区 (图片和PDF) ---
        // 关键：即使没图片也显示一个占位，方便你调试
        Text(
            text = "相关附件 (${record.imageUris.size})",
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MorandiBlue
        )

        if (record.imageUris.isEmpty()) {
            Text("暂无附件照片或PDF", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                record.imageUris.forEach { uriString ->
                    FileDetailItem(uriString)
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

// 辅助组件：信息行
@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodyMedium, color = MorandiBlue.copy(0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MorandiDark, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MorandiBlue)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MorandiDark, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun InfoSection(title: String, content: String) {
    Spacer(Modifier.height(20.dp))
    Text(title, style = MaterialTheme.typography.titleSmall, color = MorandiBlue)
    Text(content, style = MaterialTheme.typography.bodyMedium, color = MorandiDark, modifier = Modifier.padding(top = 4.dp))
}

@Composable
fun FilePreviewCard(uriString: String) {
    val isPdf = uriString.lowercase().endsWith(".pdf") || uriString.contains("pdf")

    Surface(
        modifier = Modifier.size(90.dp),
        color = MorandiBeige,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MorandiBlue.copy(0.1f))
    ) {
        if (isPdf) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MorandiRed, modifier = Modifier.size(32.dp))
                Text("PDF文件", fontSize = 10.sp, color = MorandiDark)
            }
        } else {
            // 使用 Coil 显示图片
            AsyncImage(
                model = uriString,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun FileDetailItem(uriString: String) {
    val context = LocalContext.current
    // 更加稳健的 PDF 判断逻辑
    val isPdf = uriString.lowercase().contains(".pdf") ||
            context.contentResolver.getType(Uri.parse(uriString)) == "application/pdf"

    if (isPdf) {
        // --- 核心优化：PDF 内嵌展示 ---
        var pdfBitmaps by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }

        // 在后台线程渲染 PDF
        LaunchedEffect(uriString) {
            withContext(Dispatchers.IO) {
                pdfBitmaps = com.example.sunny.util.PdfUtil.renderPdfToBitmaps(context, uriString)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MorandiRed.copy(alpha = 0.05f))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.Default.PictureAsPdf, null, tint = MorandiRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("PDF 报告预览", style = MaterialTheme.typography.labelSmall, color = MorandiRed)
            }

            if (pdfBitmaps.isEmpty()) {
                // 加载中状态
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MorandiRed.copy(alpha = 0.3f), strokeWidth = 2.dp)
                }
            } else {
                // 像照片墙一样垂直展示 PDF 的每一页
                pdfBitmaps.forEach { bitmap ->
                    Card(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        shape = RoundedCornerShape(4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        androidx.compose.foundation.Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "PDF Page",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }

                // 底部提供一个按钮，可以调用系统查看器（用于缩放和打印）
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(Uri.parse(uriString), "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "查看完整文档"))
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("查看/打印完整原件", fontSize = 12.sp, color = MorandiBlue)
                }
            }
        }
    } else {
        // --- 图片的显示方式：依然保持 AsyncImage 完整展示 ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            AsyncImage(
                model = uriString,
                contentDescription = "报告图片",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }
    }
}

@Composable
fun HealthReportTable(metrics: List<HealthMetric>) {
    val displayList = metrics.filter { it.value.isNotEmpty() }
    if (displayList.isEmpty()) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        color = White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, MorandiBlue.copy(alpha = 0.1f)),
        shadowElevation = 0.5.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            displayList.forEachIndexed { index, metric ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Top // 顶部对齐，方便多行展示参考值
                ) {
                    // 1. 左侧：指标名称（占据一半空间）
                    Text(
                        text = metric.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MorandiDark.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    )

                    // 2. 右侧：数值信息块（占据另一半空间，且内容靠右）
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End // 关键：让数字和下方信息全部右对齐
                    ) {
                        // 数值
                        Text(
                            text = metric.value,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (metric.isAbnormal()) MorandiRed else MorandiDark,
                                textAlign = TextAlign.End // 文字在内部也右对齐
                            )
                        )

                        // 单位 + 参考值（放在数字下方，不再干扰数字对齐）
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (metric.unit.isNotEmpty()) {
                                Text(
                                    text = "(${metric.unit})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray.copy(alpha = 0.6f)
                                )
                                if (metric.getRefText().isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(modifier = Modifier.size(2.dp).background(Color.LightGray, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }

                            if (metric.getRefText().isNotEmpty()) {
                                Text(
                                    text = metric.getRefText().replace("参考值: ", ""), // 简化文案，更精致
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }

                // 极细分割线
                if (index < displayList.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MorandiBlue.copy(alpha = 0.05f)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricRow(metric: HealthMetric) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：指标名称
        Text(
            text = metric.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MorandiDark.copy(alpha = 0.8f)
        )

        // 右侧：数值和单位
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleMedium,
                // 如果异常显示红色，否则显示深色
                color = if (metric.isAbnormal()) MorandiRed else MorandiDark,
                fontWeight = FontWeight.Bold
            )
            if (metric.unit.isNotEmpty()) {
                Text(
                    text = metric.unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// 抽取出来的指标输入行组件，防止代码混乱
@Composable
fun MetricInputRow(
    metric: HealthMetric,
    onUpdate: (HealthMetric) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 项目名称
        BasicTextField(
            value = metric.label,
            onValueChange = { onUpdate(metric.copy(label = it)) },
            modifier = Modifier.weight(1f).padding(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MorandiDark),
            decorationBox = { inner ->
                if (metric.label.isEmpty()) Text("项目 (如:体重)", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                inner()
            }
        )

        // 垂直分割线
        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.LightGray.copy(alpha = 0.3f)))

        // 结果数值
        BasicTextField(
            value = metric.value,
            onValueChange = { onUpdate(metric.copy(value = it)) },
            modifier = Modifier.weight(0.8f).padding(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MorandiDark, fontWeight = FontWeight.Bold),
            decorationBox = { inner ->
                if (metric.value.isEmpty()) Text("结果", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                inner()
            }
        )

        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (metric.value.isNotEmpty()) { // 只有填了值才显示图标
                Icon(
                    imageVector = if (metric.isAbnormal()) Icons.Default.Error else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (metric.isAbnormal()) MorandiRed else MorandiGreen.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 删除按钮
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun MetricEditRow(
    metric: HealthMetric,
    onUpdate: (HealthMetric) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. 项目名称 (如果是预设的，看起来像个标签；如果是自定义的，可以编辑)
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = metric.label,
                onValueChange = { onUpdate(metric.copy(label = it)) },
                label = { Text("项目", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MorandiBlue,
                    unfocusedBorderColor = MorandiBlue.copy(alpha = 0.2f)
                )
            )
        }

        // 2. 数值输入框 (重点：输入时实时判断是否异常)
        Box(modifier = Modifier.weight(0.8f)) {
            OutlinedTextField(
                value = metric.value,
                onValueChange = { onUpdate(metric.copy(value = it)) },
                label = {
                    Text(
                        text = if (metric.unit.isNotEmpty()) "数值(${metric.unit})" else "数值",
                        fontSize = 10.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    // 【高级感】：输入如果异常，文字颜色即刻变红
                    color = if (metric.isAbnormal()) MorandiRed else MorandiDark,
                    fontWeight = FontWeight.Bold
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (metric.isAbnormal()) MorandiRed else MorandiGreen,
                    unfocusedBorderColor = if (metric.isAbnormal()) MorandiRed.copy(alpha = 0.5f) else MorandiBlue.copy(alpha = 0.2f)
                )
            )
        }

        // 3. 删除小按钮
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.RemoveCircleOutline,
                contentDescription = "删除",
                tint = MorandiRed.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}