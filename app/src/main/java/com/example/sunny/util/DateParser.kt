package com.example.sunny.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object DateParser {
    // 匹配常见的日期格式：2024-01-01, 2024/01/01, 20240101 等
    private val dateRegex = Pattern.compile("(\\d{4}[\\-/.]\\d{1,2}[\\-/.]\\d{1,2})|(\\d{8})")

    fun findDateInText(text: String): Long? {
        val matcher = dateRegex.matcher(text.replace(" ", ""))
        if (matcher.find()) {
            val dateStr = matcher.group()
            val formats = listOf("yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd", "yyyyMMdd")

            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    return sdf.parse(dateStr)?.time
                } catch (e: Exception) { continue }
            }
        }
        return null
    }
}