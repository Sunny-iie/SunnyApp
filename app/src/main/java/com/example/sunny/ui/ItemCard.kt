package com.example.sunny.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sunny.data.Item
import com.example.sunny.ui.theme.*
import com.example.sunny.ui.theme.MorandiBlue
import com.example.sunny.ui.theme.White
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Card
import androidx.compose.material3.Badge
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface

import androidx.compose.ui.text.style.TextOverflow // 必须导入
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning


//@Composable
//fun ItemCard(item: Item, onClick: () -> Unit) {
//    val daysLeft = (item.expiryDate - System.currentTimeMillis()) / 86400000L
//    val sdf = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
//    val isExpired = daysLeft < 0
//    val isSoonExpiring = daysLeft in 0..3 // 3天内过期算即将过期
//
//    // 1. 根据类别和状态决定颜色
//    val (baseColor, bgColor) = when {
//        isExpired -> ExpiredGray to MorandiBeige // 已过期：灰色系
//        item.category == "食物" -> MorandiGreen to FoodBg
//        item.category == "药品" -> MorandiBlue to MedicineBg
//        else -> MorandiPink to SkinCareBg
//    }
//
//    // 2. 特殊状态的边框（即将过期时显示醒目的莫兰迪粉边框）
//    val borderStroke = if (isSoonExpiring) {
//        BorderStroke(1.5.dp, WarningRed.copy(alpha = 0.6f))
//    } else null
//
//    val themeColor = when (item.category) {
//        "食物" -> MorandiGreen
//        "药品" -> MorandiBlue
//        "护肤品", "化妆品" -> MorandiPink
//        else -> MorandiGreen
//    }
//
//    Card(
//        onClick = onClick,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 4.dp) // 减小上下外边距 (要求4)
//            .clickable { onClick() }, // 使条目可点击 (要求3)
//        colors = CardDefaults.cardColors(containerColor = White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
//            Box(modifier = Modifier.fillMaxHeight().width(5.dp).background(themeColor))
//
//            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) { // 减小内边距 (要求4)
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = item.name,
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MorandiDark,
//                        maxLines = 1, // 限制1行 (要求4)
//                        overflow = TextOverflow.Ellipsis, // 超出显示省略号 (要求4)
//                        modifier = Modifier.weight(1f) // 占满剩余空间以便截断
//                    )
//
//                    Surface(
//                        color = themeColor.copy(alpha = 0.1f),
//                        shape = RoundedCornerShape(6.dp)
//                    ) {
//                        Text(
//                            text = item.category,
//                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
//                            style = MaterialTheme.typography.labelSmall,
//                            color = themeColor
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Text(
//                    text = if (daysLeft >= 0) "还有 ${daysLeft} 天过期" else "已失效",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = if (daysLeft < 3) MorandiPink else MorandiBlue
//                )
//            }
//        }
//    }
//}

@Composable
fun ItemCard(item: Item, onClick: () -> Unit) {
    val now = System.currentTimeMillis()
    val daysLeft = (item.expiryDate - now) / 86400000L
    val isExpired = daysLeft < 0
    val isSoonExpiring = daysLeft in 0..3 // 3天内过期算即将过期
    // 计算已过期的天数（取绝对值并至少为0）
    val diff = now - item.expiryDate
    val expiredDays = (diff / 86400000L).coerceAtLeast(0)

    // 1. 根据类别和状态决定颜色
    val (baseColor, bkColor) = when {
        isExpired -> ExpiredGray to MorandiBeige // 已过期：灰色系
        item.category == "食物" -> MorandiGreen to FoodBg
        item.category == "药品" -> MorandiBlue to MedicineBg
        else -> MorandiPink to SkinCareBg
    }
    val themeColor = when {
        isExpired -> MorandiRed      // 过期统一用枯玫瑰红
        item.category == "食物" -> MorandiGreen
        item.category == "药品" -> MorandiBlue
        else -> MorandiMauve         // 护肤/化妆用灰紫色
    }

    val bgColor = when {
        isExpired -> ExpiredBg       // 过期背景
        item.category == "食物" -> FoodBg
        item.category == "药品" -> MedicineBg
        else -> BeautyBg
    }

    // 2. 边框逻辑：即将过期时增加橙色虚感边框，已过期增加红色实感边框
    val borderStroke = when {
        isExpired -> BorderStroke(1.dp, MorandiRed.copy(alpha = 0.5f))
        isSoonExpiring -> BorderStroke(1.dp, MorandiOrange.copy(alpha = 0.6f))
        else -> null
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSoonExpiring) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            // 左侧状态色条
            Box(modifier = Modifier.fillMaxHeight().width(6.dp).background(themeColor))

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium,
                                // 已过期文字加删除线
//                                textDecoration = if (isExpired) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (isExpired) MorandiRed else MorandiDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // 类别标签
                        Surface(
                            color = themeColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = item.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 右侧状态文案
                    Column(horizontalAlignment = Alignment.End) {
                        if (isExpired) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MorandiRed,
                                modifier = Modifier.size(20.dp)
                            )
//                            Text("已过期", color = MorandiRed, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                            Text(
                                text = if (expiredDays == 0L) "今天过期" else "已过期 ${expiredDays} 天",
                                color = MorandiRed,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "${daysLeft}天",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isSoonExpiring) MorandiOrange else themeColor
                            )
                            Text(
                                text = "剩余天数",
                                style = MaterialTheme.typography.labelSmall,
                                color = MorandiDark.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}