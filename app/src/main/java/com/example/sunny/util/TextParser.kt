package com.example.sunny.util

import com.example.sunny.data.HealthExamReport
import com.example.sunny.data.HealthMetric
import com.example.sunny.data.MetricType

object TextParser {
    // 报告分块配置
    private val examConfigs = listOf(
        ExamConfig("心电图检查", listOf("心电图", "ECG")),
        ExamConfig("胸部X线/DR", listOf("胸部X线", "胸部DR", "胸片", "肺部摄影")),
        ExamConfig("甲状腺超声", listOf("甲状腺彩色", "甲状腺超声", "甲状腺")),
        ExamConfig("腹部超声", listOf("肝胆胰脾", "腹部彩超", "腹部超声", "肝脏")),
        ExamConfig("泌尿系超声", listOf("泌尿系", "双肾", "膀胱超声", "输尿管"))
    )

    // --- 关键优化点：指标别名映射表 ---
    // Key 为模板中的标准名称，Value 为可能出现在体检单上的各种写法
    private val aliasMap = mapOf(
        "血红蛋白" to listOf("血红蛋白", "HGB", "Hb"),
        "红细胞" to listOf("红细胞", "RBC"),
        "平均血红蛋白浓度" to listOf("平均血红蛋白浓度", "平均红细胞血红蛋白浓度", "MCHC"),
        "平均血红蛋白含量" to listOf("平均血红蛋白含量", "平均红细胞血红蛋白含量", "MCH"),
        "红细胞平均体积" to listOf("红细胞平均体积", "平均红细胞体积", "MCV"),
        "尿蛋白" to listOf("尿蛋白", "蛋白质", "PRO"),
        "尿微白蛋白" to listOf("尿微白蛋白", "微量白蛋白", "MA"),
        "总胆红素" to listOf("总胆红素", "TBIL"),
        "直接胆红素" to listOf("直接胆红素", "DBIL"),
        "间接胆红素" to listOf("间接胆红素", "IBIL"),
        "谷氨酰转肽酶" to listOf("谷氨酰转肽酶", "γ-谷氨酰基转移酶", "GGT"),
        "RDW-CV" to listOf("RDW-CV", "红细胞分布宽度CV"),
        "血小板" to listOf("血小板", "血小板计数", "PLT"),
        "中性粒细胞百分比" to listOf("中性粒细胞百分比", "中性粒细胞比率"),
        "大型血小板比率" to listOf("大型血小板比率", "大血小板比率"),
        "淋巴细胞百分比" to listOf("淋巴细胞百分比", "淋巴细胞比率"),
        "单核细胞百分比" to listOf("单核细胞百分比", "单核细胞比率"),
        "嗜酸性粒细胞百分比" to listOf("嗜酸性粒细胞百分比", "嗜酸细胞比率"),
        "嗜碱性粒细胞百分比" to listOf("嗜碱性粒细胞百分比", "嗜碱细胞比率"),
        "中性粒细胞绝对值" to listOf("中性粒细胞绝对值", "中性粒细胞数"),
        "白细胞(WBC)" to listOf("白细胞(WBC)", "白细胞", "WBC"),
        "红细胞平均体积" to listOf("红细胞平均体积", "平均红细胞体积", "MCV"),
        "平均血红蛋白含量" to listOf("平均血红蛋白含量", "平均红细胞血红蛋白含量", "MCH"),
        "平均血红蛋白浓度" to listOf("平均血红蛋白浓度", "平均红细胞血红蛋白浓度", "MCHC"),
        "红细胞体积分布宽度" to listOf("红细胞体积分布宽度", "红细胞分布宽度SD", "RDW-SD"),
        "平均血小板体积" to listOf("平均血小板体积", "血小板平均体积", "MPV"),
        "淋巴细胞绝对值" to listOf("淋巴细胞绝对值", "淋巴细胞数"),
        "嗜酸性粒细胞绝对值" to listOf("嗜酸性粒细胞绝对值", "嗜酸细胞数"),
        "单核细胞绝对值" to listOf("单核细胞绝对值", "单核细胞数"),
        "嗜碱性粒细胞绝对值" to listOf("嗜碱性粒细胞绝对值", "嗜碱细胞数"),
        "高密度胆固醇" to listOf("高密度胆固醇", "高密度脂蛋白胆固醇", "HDL-C"),
        "低密度胆固醇" to listOf("低密度胆固醇", "低密度脂蛋白胆固醇", "LDL-C"),
        "尿比重测定(USG)" to listOf("尿比重测定(USG)", "比重", "USG", "SG"),
        "尿酸碱度检查(UPH)" to listOf("尿酸碱度检查(UPH)", "酸碱度", "PH"),
        "尿胆原定性试验(UBG)" to listOf("尿胆原定性试验(UBG)", "尿胆原", "UBG"),
        "尿糖定性试验(UGLU)" to listOf("尿糖定性试验(UGLU)", "葡萄糖", "尿糖", "UGLU"),
        "尿酮体定性试验(KETU)" to listOf("尿酮体定性试验(KETU)", "酮体", "KET", "KETU"),
        "尿胆红素(UBIL)" to listOf("尿胆红素(UBIL)", "胆红素", "UBIL", "BIL"),
        "尿蛋白定性试验(UPRO)" to listOf("尿蛋白定性试验(UPRO)", "蛋白质", "尿蛋白", "PRO", "UPRO"),
        "亚硝酸盐(UNIT)" to listOf("亚硝酸盐(UNIT)", "亚硝酸盐", "NIT"),
        "尿隐血" to listOf("尿隐血", "潜血", "尿红细胞", "BLD"),
        "白细胞(ULEU)" to listOf("白细胞(ULEU)", "尿白细胞酯酶", "LEU", "ULEU")
    )


    data class ExamConfig(val standardName: String, val keywords: List<String>)

    fun parseAll(text: String, currentMetrics: List<HealthMetric>): Pair<List<HealthMetric>, List<HealthExamReport>> {
        val reports = mutableListOf<HealthExamReport>()
        val updatedMetrics = currentMetrics.toMutableList()
        val allStandardLabels = currentMetrics.map { it.label }
        val allKnownKeywords = (aliasMap.values.flatten() + currentMetrics.map { it.label }).distinct()

        // --- 步骤 1: 将文本拆分为独立的检查块 ---
        // 逻辑：识别文中所有可能的检查标题位置
        val allKeywords = examConfigs.flatMap { it.keywords }
        val foundPositions = mutableListOf<Pair<Int, ExamConfig>>()

        examConfigs.forEach { config ->
            config.keywords.forEach { kw ->
                var index = text.indexOf(kw)
                while (index != -1) {
                    foundPositions.add(index to config)
                    index = text.indexOf(kw, index + 1)
                }
            }
        }
        val sortedPositions = foundPositions.distinctBy { it.second.standardName }.sortedBy { it.first }

        // --- 步骤 2: 在每个块内部精准抓取 ---
        for (i in sortedPositions.indices) {
            val start = sortedPositions[i].first
            val end = if (i + 1 < sortedPositions.size) sortedPositions[i + 1].first else text.length
            val blockText = text.substring(start, end) // 拿到当前检查的局部文本
            val config = sortedPositions[i].second

            val findings = extractLocal(blockText, "检查所见")
            val conclusion = extractLocal(blockText, "检查结论")

            if (findings.isNotEmpty() || conclusion.isNotEmpty()) {
                reports.add(HealthExamReport(config.standardName, findings, conclusion))
            }
        }

        // --- 步骤 3: 解析数值/短指标，并进行“去重”和“长度过滤” ---
        val lines = text.lines()
        lines.forEach { line ->
            val cleanLine = line.replace(" ", "")
            if (cleanLine.isBlank()) return@forEach

            updatedMetrics.forEachIndexed { index, metric ->
                // 获取当前指标对应的所有“马甲”（别名）
                val keywords = aliasMap[metric.label] ?: listOf(metric.label)

                // 查找行内是否包含其中任何一个别名
                val matchedKeyword = keywords.find { cleanLine.contains(it, ignoreCase = true) }

                if (matchedKeyword != null && !cleanLine.contains("检查")) {

                    // 冲突检测：避让更长的指标名（解决“血小板”vs“血小板分布宽度”）
                    val hasBetterMatch = allKnownKeywords.any { other ->
                        other.length > matchedKeyword.length &&
                                other.contains(matchedKeyword) &&
                                cleanLine.contains(other)
                    }
                    if (hasBetterMatch) return@forEachIndexed

                    // 提取数值
                    var rawValue = cleanLine.substringAfter(matchedKeyword).trim()

                    if (metric.type == MetricType.NUMERIC) {
                        val numberRegex = "(\\d+\\.?\\d*)".toRegex()
                        val match = numberRegex.find(rawValue)
                        if (match != null) {
                            // 只有当新抓到的值不为空时才更新，实现增量填充
                            updatedMetrics[index] = updatedMetrics[index].copy(value = match.value)
                        }
                    } else {
                        var cleanValue = rawValue
                            .replace(Regex("^[:：\\s脏]+"), "")
                            .replace(Regex("[↑↓]+"), "")
                            .trim()

                        if (cleanValue.length in 1..12) {
                            updatedMetrics[index] = updatedMetrics[index].copy(value = cleanValue)
                        }
                    }
                }
            }
        }

        return Pair(updatedMetrics, reports)
    }

    private fun extractLocal(blockText: String, target: String): String {
        // 在局部块内寻找，防止抓到下一个项目的结论
        val pattern = "$target\\s*[:：]?\\s*([\\s\\S]*?)(?=(检查结论|检查所见|$))".toRegex()
        return pattern.find(blockText)?.groupValues?.get(1)?.trim() ?: ""
    }
}