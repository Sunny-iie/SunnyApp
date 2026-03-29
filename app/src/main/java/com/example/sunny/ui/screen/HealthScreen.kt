package com.example.sunny.ui.screen

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.horizontalScroll
import com.example.sunny.data.HealthRecord
import com.example.sunny.data.ItemDao
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.example.sunny.showDatePicker
import com.example.sunny.ui.theme.MorandiBeige
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sunny.ui.theme.MorandiRed


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
                        Text("🖼️ ${record.imageUris.size}张图片", style = MaterialTheme.typography.labelSmall)
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

    // 1. 修改选择器，支持所有图片和 PDF
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            // 关键：申请永久保留该文件的访问权限
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
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

        // 2. 疾病分组与医院输入
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = diseaseName,
                onValueChange = { diseaseName = it },
                label = { Text("疾病所属分组 (如:胃病)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = hospitalName,
                onValueChange = { hospitalName = it },
                label = { Text("就诊医院") },
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
                onClick = {
                    launcher.launch(arrayOf("image/*", "application/pdf"))
                },
                // 样式代码...
            ) {
                Icon(Icons.Default.AttachFile, null)
                Text("添加照片或PDF报告")
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
                        isDeleted = initial?.isDeleted ?: false,
                        diseaseName = diseaseName,
                        hospitalName = hospitalName,
                        imageUris = imageUris
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
        // --- PDF 的显示方式：做一个精致的文件卡片 ---
        Card(
            onClick = {
                // 点击逻辑：调用系统应用打开 PDF
                try {
                    val uri = Uri.parse(uriString)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 授权读取
                    }
                    context.startActivity(Intent.createChooser(intent, "打开 PDF 报告"))
                } catch (e: Exception) {
                    // 如果手机没安装任何 PDF 阅读器
                    android.widget.Toast.makeText(context, "未找到 PDF 阅读器", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp), // PDF 显示为横向的长条卡片
            colors = CardDefaults.cardColors(containerColor = MorandiRed.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, MorandiRed.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PDF 专属图标
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF",
                    tint = MorandiRed,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("医疗报告.pdf", style = MaterialTheme.typography.bodyMedium, color = MorandiDark)
                    Text("点击使用系统阅读器查看详情", style = MaterialTheme.typography.labelSmall, color = MorandiDark.copy(alpha = 0.5f))
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


