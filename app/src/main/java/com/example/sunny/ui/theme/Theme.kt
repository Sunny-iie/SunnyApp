package com.example.sunny.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 确保这里的颜色变量在你的 Color.kt 中已定义
private val LightColorScheme = lightColorScheme(
    primary = MorandiGreen,
    secondary = MorandiBlue,
    background = MorandiBeige,
    surface = White,
    onPrimary = White,
    onBackground = MorandiDark,
    onSurface = MorandiDark
)

@Composable
fun SunnyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}