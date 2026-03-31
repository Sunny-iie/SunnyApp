package com.example.sunny.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions

object MetricScanner {
    private val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    // 数字类项目
    private val numericMetrics = listOf("体重", "身高", "心率", "收缩压", "舒张压", "BMI", "体重指数", "腰围", "臀围", "腰臀比","丙氨酸基转移酶",
        "天门冬氨酸氨基转移酶", "谷氨酰转肽酶", "总胆红素", "直接胆红素",
        "总蛋白", "白蛋白", "球蛋白", "A/G", "甲胎蛋白", "肌酐", "尿素",
        "尿酸", "葡萄糖", "癌胚抗原", "血红蛋白", "红细胞压积", "红细胞",
        "白细胞(WBC)", "血小板", "总胆固醇", "甘油三酯", "高密度胆固醇",
        "低密度胆固醇", "尿酸碱度检查(UPH)", "尿比重测定(USG)", "维生素C(VC)",
        "镜检红细胞", "镜检白细胞")

    // 关键字映射
    private val keywordMap = mapOf(
        "甲状腺" to listOf("甲状腺"),
        "神经系统" to listOf("神经系统"),
        "腹部压痛" to listOf("腹部压痛", "压痛"),
        "腹部包块" to listOf("腹部包块", "包块"),
        "肠鸣音" to listOf("肠鸣音"),
        "双肾" to listOf("双肾"),
        "脾" to listOf("脾"),
        "肝" to listOf("肝"),
        "肺部" to listOf("肺部"),
        "心脏杂音" to listOf("心脏杂音"),
        "心律" to listOf("心律"),
        "心率" to listOf("心率", "脉搏"),
        "体重" to listOf("体重"),
        "身高" to listOf("身高"),
        "收缩压" to listOf("收缩压", "高压"),
        "舒张压" to listOf("舒张压", "低压"),
        "BMI" to listOf("BMI", "体质指数","体重指数"),
        "药物过敏史" to listOf("药物过敏", "过敏史"),
        "既往史" to listOf("既往史"),
        "外眼" to listOf("外眼"),
        "皮肤" to listOf("皮肤"),
        "脊柱" to listOf("脊柱"),
        "丙氨酸基转移酶" to listOf("丙氨酸基转移酶", "ALT", "GPT"),
        "天门冬氨酸氨基转移酶" to listOf("天门冬氨酸氨基转移酶", "AST", "GOT"),
        "谷氨酰转肽酶" to listOf("谷氨酰转肽酶", "GGT"),
        "总胆红素" to listOf("总胆红素", "TBIL"),
        "直接胆红素" to listOf("直接胆红素", "DBIL"),
        "总蛋白" to listOf("总蛋白", "TP"),
        "白蛋白" to listOf("白蛋白", "ALB"),
        "甲胎蛋白" to listOf("甲胎蛋白", "AFP"),
        "肌酐" to listOf("肌酐", "CREA", "CRE"),
        "尿素" to listOf("尿素", "UREA"),
        "尿酸" to listOf("尿酸", "UA"),
        "葡萄糖" to listOf("葡萄糖", "GLU", "血糖"),
        "癌胚抗原" to listOf("癌胚抗原", "CEA"),
        "血红蛋白" to listOf("血红蛋白", "HGB", "Hb"),
        "白细胞(WBC)" to listOf("白细胞", "WBC"),
        "红细胞" to listOf("红细胞", "RBC"),
        "血小板" to listOf("血小板", "PLT"),
        "总胆醇" to listOf("总胆固醇", "TC", "CHO"),
        "甘油三酯" to listOf("甘油三酯", "TG"),
        "高密度胆固醇" to listOf("高密度", "HDL"),
        "低密度胆固醇" to listOf("低密度", "LDL"),
        "尿酸碱度检查(UPH)" to listOf("酸碱度", "PH"),
        "尿比重测定(USG)" to listOf("比重", "SG")
    )

    fun scanImage(context: Context, uri: Uri, onResult: (Map<String, String>) -> Unit) {
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            onResult(emptyMap())
            return
        }

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val results = mutableMapOf<String, String>()
                val allLines = visionText.textBlocks.flatMap { it.lines }
                val numberRegex = "(\\d+\\.?\\d*)".toRegex()

                keywordMap.forEach { (metricLabel, keywords) ->
                    // 1. 过滤标题干扰，寻找真正的标签行
                    val candidateLines = allLines.filter { line ->
                        keywords.any { line.text.contains(it) } && line.text.length < 15
                    }.sortedBy { it.boundingBox?.top ?: 0 }

                    for (labelLine in candidateLines) {
                        val labelRect = labelLine.boundingBox ?: continue
                        val labelCenterY = labelRect.centerY()
                        val labelRight = labelRect.right
                        val isNumeric = metricLabel in numericMetrics

                        // --- 逻辑：寻找右侧同行文字 ---
                        val valueLine = allLines.filter { line ->
                            val text = line.text.replace(" ", "")
                            val rect = line.boundingBox ?: return@filter false

                            val isNoise = text.contains("参考") || text.contains("范围") ||
                                    text.contains("首页") || keywords.any { text == it }

                            // 垂直偏移容差缩小到 35 像素，防止跨行
                            val isSameRow = Math.abs(rect.centerY() - labelCenterY) < 35
                            // 必须在标签右侧至少 10 像素
                            val isAtRight = rect.left > labelRight + 10

                            !isNoise && isSameRow && isAtRight
                        }.minByOrNull { it.boundingBox?.left ?: Int.MAX_VALUE }

                        if (valueLine != null) {
                            var rawVal = valueLine.text.trim().trim(':', '：')

                            // 【自动纠错层】：处理常见医疗 OCR 错误
                            rawVal = rawVal.replace("朱见", "未见")
                                .replace("腹都", "无")
                                .replace("否习", "否认")

                            if (isNumeric) {
                                val match = numberRegex.find(rawVal)
                                if (match != null) {
                                    results[metricLabel] = match.value
                                    break
                                }
                            } else if (rawVal.isNotEmpty()) {
                                results[metricLabel] = rawVal
                                break
                            }
                        }
                    }
                }
                onResult(results)
            }
            .addOnFailureListener { onResult(emptyMap()) }
    }
}