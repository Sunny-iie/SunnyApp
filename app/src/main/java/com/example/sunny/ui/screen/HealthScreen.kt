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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PostAdd
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sunny.data.HealthExamReport
import com.example.sunny.data.HealthMetric
import com.example.sunny.data.HealthTemplates
import com.example.sunny.data.MetricType
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
                    letterSpacing = 1.sp,
                    color = MorandiBlue
                ),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                color = MorandiBlue
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
    var pasteText by remember { mutableStateOf("") } // 粘贴板文字
    var examReports by remember {
        mutableStateOf(initial?.examReports ?: emptyList<HealthExamReport>())
    }

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
    // 使用 Box 容器来叠加“滚动层”和“固定层”
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f) // 保持弹窗高度
            .background(White)
    ) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            // 关键：给底部留出足够的 padding，防止内容被固定的按钮遮挡
            .padding(bottom = 100.dp)
            .verticalScroll(rememberScrollState())
            .imePadding() // 避让键盘
    ) {
        Text(
            text = if (initial == null) "✨ 录入新记录" else "✏️ 编辑信息",
            style = MaterialTheme.typography.headlineSmall,
            color = MorandiDark,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // 1. 标题输入
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = {
                Text(
                    "记录名称 (如：XX医院年度体检)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                )
            },
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
                Text("发生日期", style = MaterialTheme.typography.labelMedium, color = MorandiBlue)
                Text(sdf.format(Date(date)), color = MorandiBlue, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. 医生与费用 (并排展示)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = doctor,
                onValueChange = { doctor = it },
                label = {
                    Text(
                        "主治医生",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = {
                    Text(
                        "花费(元)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                    )
                },
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
                label = {
                    Text(
                        "分组 (如:消化系统)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MorandiBlue
                    )
                },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = hospitalName,
                onValueChange = { hospitalName = it },
                label = {
                    Text(
                        "就诊医院",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (type == "体检") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .background(MorandiBlue.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        null,
                        tint = MorandiBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "AI 智能填表",
                        style = MaterialTheme.typography.labelMedium,
                        color = MorandiBlue
                    )
                }

                Spacer(Modifier.height(8.dp))

                // 窄窄的输入框
                OutlinedTextField(
                    value = pasteText,
                    onValueChange = { pasteText = it },
                    placeholder = {
                        Text(
                            "在此粘贴 AI 总结的指标数据...",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 100.dp), // 限制高度，不占地方
                    textStyle = MaterialTheme.typography.bodySmall,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MorandiBlue,
                        unfocusedBorderColor = MorandiBlue.copy(alpha = 0.2f),
                        // 将 containerColor 修改为下面这两个参数
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    // 增加一键解析按钮
                    trailingIcon = {
                        if (pasteText.isNotEmpty()) {
                            IconButton(onClick = {
                                if (pasteText.isNotBlank()) {
                                    val base =
                                        if (metrics.isEmpty()) HealthTemplates.fullCheckupMetrics else metrics

                                    // 调用新的 Pair 解析方法
                                    val (newMetrics, newReports) = com.example.sunny.util.TextParser.parseAll(
                                        pasteText,
                                        base
                                    )

                                    metrics = newMetrics
                                    examReports = newReports

                                    pasteText = ""
                                    Toast.makeText(context, "已智能分类填入", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }) {
                                Icon(Icons.Default.CheckCircle, null, tint = MorandiGreen)
                            }
                        }
                    }
                )

                Text(
                    "提示：粘贴“指标 结果”格式文字，点击对勾自动填表",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            // 2. 指标表格区 (Metrics)
            Text(
                "1. 基础指标明细",
                style = MaterialTheme.typography.titleSmall,
                color = MorandiDark,
                modifier = Modifier.padding(top = 16.dp)
            )
            val editGroups = metrics.groupBy { it.category }
            editGroups.forEach { (catName, catItems) ->
                Text(
                    catName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MorandiBlue,
                    modifier = Modifier.padding(top = 8.dp)
                )
                catItems.forEach { metric ->
                    MetricEditRow(
                        metric = metric,
                        onUpdate = { updated ->
                            val newList = metrics.toMutableList()
                            val index = metrics.indexOf(metric)
                            if (index != -1) {
                                newList[index] = updated; metrics = newList
                            }
                        },
                        onDelete = {
                            val newList = metrics.toMutableList()
                            newList.remove(metric)
                            metrics = newList
                        }
                    )
                }
            }
            // 【新增】手动添加指标按钮
            TextButton(onClick = {
                metrics = metrics + HealthMetric("自定义", "新项目", "", "", type = MetricType.TEXT)
            }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Text("增加指标行", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 大型报告区 (Exam Reports)
            Text(
                "2. 影像功能报告",
                style = MaterialTheme.typography.titleSmall,
                color = MorandiDark
            )
            examReports.forEachIndexed { index, report ->
                ExamReportEditCard(
                    report = report,
                    onUpdate = { updated ->
                        val newList = examReports.toMutableList()
                        newList[index] = updated
                        examReports = newList
                    },
                    onDelete = {
                        val newList = examReports.toMutableList()
                        newList.removeAt(index)
                        examReports = newList
                    }
                )
            }
            // 【新增】手动添加报告按钮
            OutlinedButton(
                onClick = { examReports = examReports + HealthExamReport("新检查项目", "", "") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MorandiBlue.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.PostAdd, null)
                Spacer(Modifier.width(8.dp))
                Text("手动添加一项检查报告")
            }
        }


        // 3. 图片展示与上传区
        Text("报告及照片附件", style = MaterialTheme.typography.labelSmall, color = MorandiBlue)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
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
                            Icon(
                                Icons.Default.PictureAsPdf,
                                null,
                                tint = MorandiRed,
                                modifier = Modifier.size(28.dp)
                            )
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
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                            .background(Color.Black.copy(0.3f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

        }
        Spacer(modifier = Modifier.height(24.dp))


        // 5. 详情记录
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = {
                Text(
                    "详情/体检结论/医生嘱托",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))



        Spacer(modifier = Modifier.height(24.dp))
    }
        // --- 第二层：固定的吸底按钮区 ---
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = White,
            tonalElevation = 8.dp, // 增加微弱的阴影，营造悬浮感
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding() // 避开手机底部横条
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(HealthRecord(
                                id = initial?.id ?: 0,
                                title = title,
                                type = type,
                                diseaseName = diseaseName,
                                hospitalName = hospitalName,
                                doctorName = doctor,
                                date = date,
                                content = content,
                                medication = medication,
                                cost = cost,
                                imageUris = imageUris,
                                metrics = metrics,
                                examReports = examReports,
                                isDeleted = initial?.isDeleted ?: false
                            ))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MorandiGreen),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("确认保存健康记录", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
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
        // --- 核心信息卡片 (2x2 紧凑布局) ---
        Surface(
            color = MorandiBlue.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp) // 行间距
            ) {
                // 第一行：时间 和 医院
                Row(modifier = Modifier.fillMaxWidth()) {
                    CompactInfoItem(
                        label = "就诊时间",
                        value = sdf.format(Date(record.date)),
                        modifier = Modifier.weight(1f)
                    )
                    CompactInfoItem(
                        label = "就诊医院",
                        value = record.hospitalName.ifEmpty { "未记录" },
                        modifier = Modifier.weight(1.2f) // 医院名字可能较长，给多一点权重
                    )
                }

                // 分割细线 (可选，增加精致感)
                HorizontalDivider(thickness = 0.5.dp, color = MorandiBlue.copy(alpha = 0.1f))

                // 第二行：医生 和 费用
                Row(modifier = Modifier.fillMaxWidth()) {
                    CompactInfoItem(
                        label = "主治医生",
                        value = record.doctorName.ifEmpty { "未记录" },
                        modifier = Modifier.weight(1f)
                    )
                    CompactInfoItem(
                        label = "产生费用",
                        value = if(record.cost.isEmpty()) "¥ 0" else "¥ ${record.cost}",
                        modifier = Modifier.weight(1.2f)
                    )
                }
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

        // 2. 展示大型检查报告（方框形式）
        if (record.examReports.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("影像功能报告", style = MaterialTheme.typography.titleSmall, color = MorandiBlue, modifier = Modifier.padding(vertical = 8.dp))

            record.examReports.forEach { report ->
                ExamReportCard(report)
                Spacer(Modifier.height(12.dp))
            }
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
    // 1. 过滤掉空值，并按 category 分组
    val groupedMetrics = metrics.filter { it.value.isNotBlank() }.groupBy { it.category }

    if (groupedMetrics.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp) // 每个分类卡片之间的间距
    ) {
        groupedMetrics.forEach { (categoryName, items) ->
            // --- 分类区块卡片 ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, MorandiBlue.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    // 分类标题栏（带淡淡的底色背景）
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MorandiBlue.copy(alpha = 0.05f)
                    ) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MorandiBlue,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    // 遍历该分类下的指标
                    items.forEachIndexed { index, metric ->
                        CompactMetricRow(metric)
                        // 分行线：最后一项不显示
                        if (index < items.size - 1) {
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
    }
}

@Composable
fun CompactMetricRow(metric: HealthMetric) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically // 居中对齐让单行更整齐
    ) {
        // 左侧：指标名称
        Text(
            text = metric.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MorandiDark.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )

        // 右侧：数值 + 详情信息（垂直排布）
        Column(horizontalAlignment = Alignment.End) {
            // 第一层：数值
            Text(
                text = metric.value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (metric.isAbnormal) MorandiRed else MorandiDark
                )
            )

            // 第二层：单位和参考值合并（极小字号）
            val detailText = buildString {
                if (metric.unit.isNotEmpty()) append(metric.unit)
                if (metric.refText.isNotEmpty()) {
                    if (isNotEmpty()) append(" | ")
                    append(metric.refText.replace("参考值: ", ""))
                }
            }

            if (detailText.isNotBlank()) {
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color.LightGray,
                    maxLines = 1,
                    softWrap = false
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
                    imageVector = if (metric.isAbnormal) Icons.Default.Error else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (metric.isAbnormal) MorandiRed else MorandiGreen.copy(alpha = 0.6f),
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
                    color = if (metric.isAbnormal) MorandiRed else MorandiDark,
                    fontWeight = FontWeight.Bold
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (metric.isAbnormal) MorandiRed else MorandiGreen,
                    unfocusedBorderColor = if (metric.isAbnormal) MorandiRed.copy(alpha = 0.5f) else MorandiBlue.copy(alpha = 0.2f)
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

@Composable
fun HealthExamReportSection(reports: List<HealthExamReport>) {
    if (reports.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "影像/功能检查报告",
            style = MaterialTheme.typography.titleSmall,
            color = MorandiBlue,
            modifier = Modifier.padding(top = 8.dp)
        )

        reports.forEach { report ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MorandiBlue.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 1. 报告标题 (如：十二通道常规心电图检查)
                    Text(
                        text = report.examName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MorandiBlue
                    )

                    // 2. 检查所见 (灰色小字标签 + 描述)
                    if (report.findings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("【检查所见】", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = report.findings,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MorandiDark.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    }

                    // 3. 检查结论 (重点：MorandiBlue 强调)
                    if (report.conclusion.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MorandiBlue.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text("【检查结论】", style = MaterialTheme.typography.labelSmall, color = MorandiBlue)
                                Text(
                                    text = report.conclusion,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MorandiDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamReportCard(report: HealthExamReport) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MorandiBlue.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 报告名称
            Text(report.examName, style = MaterialTheme.typography.titleMedium, color = MorandiBlue, fontWeight = FontWeight.Bold)

            if (report.findings.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("【检查所见】", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(report.findings, style = MaterialTheme.typography.bodySmall, color = MorandiDark, lineHeight = 18.sp)
            }

            if (report.conclusion.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MorandiBlue.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text("【检查结论】", style = MaterialTheme.typography.labelSmall, color = MorandiBlue)
                        Text(report.conclusion, style = MaterialTheme.typography.bodySmall, color = MorandiDark, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ExamReportEditCard(
    report: HealthExamReport,
    onUpdate: (HealthExamReport) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        color = MorandiBlue.copy(alpha = 0.02f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MorandiBlue.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 检查名称编辑
                BasicTextField(
                    value = report.examName,
                    onValueChange = { onUpdate(report.copy(examName = it)) },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.titleSmall.copy(color = MorandiBlue, fontWeight = FontWeight.Bold),
                    decorationBox = { inner ->
                        if (report.examName.isEmpty()) Text("检查项目名称", color = Color.LightGray, style = MaterialTheme.typography.titleSmall)
                        inner()
                    }
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, tint = MorandiRed.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 检查所见编辑
            Text("检查所见", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            OutlinedTextField(
                value = report.findings,
                onValueChange = { onUpdate(report.copy(findings = it)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 检查结论编辑
            Text("检查结论", style = MaterialTheme.typography.labelSmall, color = MorandiBlue)
            OutlinedTextField(
                value = report.conclusion,
                onValueChange = { onUpdate(report.copy(conclusion = it)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MorandiBlue.copy(alpha = 0.1f), focusedContainerColor = Color.White)
            )
        }
    }
}

@Composable
fun CompactInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MorandiBlue.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MorandiDark,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis // 医院名太长自动省略
        )
    }
}