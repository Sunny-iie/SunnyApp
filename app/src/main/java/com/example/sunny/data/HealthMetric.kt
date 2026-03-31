package com.example.sunny.data

// 1. 定义项目类型
enum class MetricType {
    NUMERIC, // 数值型（如身高、体重）
    TEXT     // 文本型（如病史、描述）
}

data class HealthMetric(
    val category: String,
    val label: String,
    var value: String = "",
    val unit: String = "",
    val refMin: Double? = null,
    val refMax: Double? = null,
    val type: MetricType = MetricType.TEXT // 2. 默认设为文本型
) {
    val isAbnormal: Boolean
        get() {
            if (type == MetricType.TEXT) return false // 文本型暂不自动判断异常
            val num = value.toDoubleOrNull() ?: return false
            if (refMin != null && num < refMin) return true
            if (refMax != null && num > refMax) return true
            return false
        }

    val refText: String
        get() = when {
            refMin != null && refMax != null -> "${"%.2f".format(refMin)} ~ ${"%.2f".format(refMax)}"
            refMin != null -> "> ${"%.2f".format(refMin)}"
            refMax != null -> "< ${"%.2f".format(refMax)}"
            else -> ""
        }
}

// data/HealthMetric.kt

object HealthTemplates {
    val fullCheckupMetrics = listOf(

        // 4. 一般身体检查 (本身大部分就是空的
        HealthMetric("一般身体检查", "体重", "", "Kg", type=MetricType.NUMERIC),
        HealthMetric("一般身体检查", "身高", "", "cm", type=MetricType.NUMERIC),
        HealthMetric("一般身体检查", "舒张压", "", "mmHg", 60.0, 80.0, type=MetricType.NUMERIC),
        HealthMetric("一般身体检查", "收缩压", "", "mmHg", 90.0, 120.0, type=MetricType.NUMERIC),
        HealthMetric("一般身体检查", "BMI", "", "", 18.5, 24.0, MetricType.NUMERIC),
        HealthMetric("一般身体检查", "臀围", "", "CM", type=MetricType.NUMERIC),
        HealthMetric("一般身体检查", "腰围", "", "CM", 0.0, 85.0, MetricType.NUMERIC),

        // 1. 甲状腺彩超 - value 设为空
        HealthMetric("甲状腺彩超", "甲状腺", "", type = MetricType.TEXT),

        // 5. 眼科
        HealthMetric("眼科", "外眼(左)", "", type = MetricType.TEXT),
        HealthMetric("眼科", "外眼(右)", "", type = MetricType.TEXT),
        HealthMetric("眼科", "辨色力", "", type = MetricType.TEXT),
        HealthMetric("眼科", "矫正视力(左)", "", type=MetricType.NUMERIC),
        HealthMetric("眼科", "矫正视力(右)", "", type=MetricType.NUMERIC),
        HealthMetric("眼科", "裸眼视力(左)", "", type = MetricType.NUMERIC),
        HealthMetric("眼科", "裸眼视力(右)", "", type = MetricType.NUMERIC),
        HealthMetric("眼科", "裂隙灯", "", type = MetricType.TEXT),
        HealthMetric("眼科", "眼睑", "", type = MetricType.TEXT),
        HealthMetric("眼科", "巩膜", "", type = MetricType.TEXT),
        // 8. 肝功2号
        HealthMetric("肝功2号", "丙氨酸氨基转移酶", "", "U/L", 0.0, 40.0, MetricType.NUMERIC),
        HealthMetric("肝功2号", "天门冬氨酸氨基转移酶", "", "U/L", 0.0, 40.0, MetricType.NUMERIC),
        HealthMetric("肝功2号", "谷氨酰转肽酶", "", "U/L", 0.0, 50.0, MetricType.NUMERIC),
        HealthMetric("肝功2号", "总胆红素", "", "μmol/L", 0.0, 20.0, MetricType.NUMERIC),
        HealthMetric("肝功2号", "直接胆红素", "", "μmol/L", 0.0, 7.0, MetricType.NUMERIC),
        HealthMetric("肝功2号", "间接胆红素", "", "μmol/L", 0.0, 17.0, MetricType.NUMERIC),
        HealthMetric("肝功2号", "总蛋白", "", "g/L", 60.0, 85.0, MetricType.NUMERIC),
        HealthMetric("肝功2号", "白蛋白", "", "g/L", 35.0, 55.0, MetricType.NUMERIC),
        HealthMetric("肝功2号", "球蛋白", "", "g/L", 20.0, 45.0, MetricType.NUMERIC),
        HealthMetric("肝功2号", "A/G", "", "-", 1.0, 2.5, MetricType.NUMERIC),
        HealthMetric("肝功2号", "谷草/谷丙", "", "-", 1.0, 1.5, MetricType.NUMERIC),       // AST/ALT 比值
        HealthMetric("肝功2号", "碱性磷酸酶", "", "U/L", 35.0, 100.0, MetricType.NUMERIC),  // ALP
        HealthMetric("肝功2号", "胆汁酸", "", "μmol/L", 0.0, 12.0, MetricType.NUMERIC),       // TBA

        // 9. 肾功与代谢
        HealthMetric("肿瘤标志物", "甲胎蛋白", "", "ng/ml", 0.0, 7.0, MetricType.NUMERIC),
        HealthMetric("肾功能", "肌酐", "", "μmol/L", 45.0, 104.0, MetricType.NUMERIC),
        HealthMetric("肾功能", "尿素", "", "mmol/L", 1.7, 8.3, MetricType.NUMERIC),
        HealthMetric("肾功能", "尿酸", "", "μmol/L", 143.0, 463.0, MetricType.NUMERIC),
        HealthMetric("肾功能", "肾小球滤过率", "", "%", type= MetricType.NUMERIC),
        HealthMetric("血糖", "葡萄糖", "", "mmol/L", 3.9, 6.1, MetricType.NUMERIC),
        HealthMetric("肿瘤标志物", "癌胚抗原", "", "ng/ml", 0.0, 3.4, MetricType.NUMERIC),

        // 10. 全血细胞分析(五分类)
        HealthMetric("血常规", "血红蛋白", "", "g/L", 115.0, 150.0, MetricType.NUMERIC),
        HealthMetric("血常规", "红细胞压积", "", "%", 35.0, 45.0, MetricType.NUMERIC),
        HealthMetric("血常规", "红细胞", "", "10^12/L", 3.8, 5.1, MetricType.NUMERIC),
        HealthMetric("血常规", "白细胞(WBC)", "", "10^9/L", 3.5, 9.5, MetricType.NUMERIC),
        HealthMetric("血常规", "红细胞平均体积", "", "fL", 82.0, 100.0, MetricType.NUMERIC),
        HealthMetric("血常规", "平均血红蛋白含量", "", "pg", 27.0, 34.0, MetricType.NUMERIC),
        HealthMetric("血常规", "平均血红蛋白浓度", "", "g/L", 316.0, 354.0, MetricType.NUMERIC),
        HealthMetric("血常规", "RDW-CV", "", "%", 10.9, 15.4, MetricType.NUMERIC),
        HealthMetric("血常规", "红细胞体积分布宽度", "", "fL", 31.0, 51.0, MetricType.NUMERIC),
        HealthMetric("血常规", "血小板", "", "10^9/L", 125.0, 350.0, MetricType.NUMERIC),
        HealthMetric("血常规", "淋巴细胞绝对值", "", "10^9/L", 1.1, 3.2, MetricType.NUMERIC),
        HealthMetric("血常规", "单核细胞绝对值", "", "10^9/L", 0.1, 0.6, MetricType.NUMERIC),
        HealthMetric("血常规", "中性粒细胞绝对值", "", "10^9/L", 1.8, 6.3, MetricType.NUMERIC),
        HealthMetric("血常规", "嗜酸性粒细胞绝对值", "", "10^9/L", 0.02, 0.52, MetricType.NUMERIC),
        HealthMetric("血常规", "嗜碱性粒细胞绝对值", "", "10^9/L", 0.0, 0.06, MetricType.NUMERIC),
        HealthMetric("血常规", "淋巴细胞百分比", "", "%", 20.0, 50.0, MetricType.NUMERIC),
        HealthMetric("血常规", "单核细胞百分比", "", "%", 3.0, 10.0, MetricType.NUMERIC),
        HealthMetric("血常规", "中性粒细胞百分比", "", "%", 40.0, 75.0, MetricType.NUMERIC),
        HealthMetric("血常规", "嗜酸性粒细胞百分比", "", "%", 0.4, 8.0, MetricType.NUMERIC),
        HealthMetric("血常规", "嗜碱性粒细胞百分比", "", "%", 0.0, 1.0, MetricType.NUMERIC),
        HealthMetric("血常规", "血小板压积", "", "%", 0.11, 0.28, MetricType.NUMERIC),
        HealthMetric("血常规", "大型血小板比率", "", "%", 13.0, 43.0, MetricType.NUMERIC),
        HealthMetric("血常规", "平均血小板体积", "", "fL", 8.0, 12.0, MetricType.NUMERIC),
        HealthMetric("血常规", "血小板分布宽度", "", "fL", 9.0, 17.0, MetricType.NUMERIC),
        HealthMetric("血常规", "空腹血糖", "", "fL", 3.89, 6.11, MetricType.NUMERIC),

        // 11. 血脂四项
        HealthMetric("血脂", "总胆固醇", "", "mmol/L", 2.82, 5.69, MetricType.NUMERIC),
        HealthMetric("血脂", "甘油三酯", "", "mmol/L", 0.4, 1.56, MetricType.NUMERIC),
        HealthMetric("血脂", "高密度胆固醇", "", "mmol/L", 1.29, 1.55, MetricType.NUMERIC),
        HealthMetric("血脂", "低密度胆固醇", "", "mmol/L", 2.70, 3.10, MetricType.NUMERIC),

        // 12. 尿常规
        HealthMetric("尿常规", "透明管型(UTAST)", "", "个/LP", 0.0, 2.0, MetricType.NUMERIC),
        HealthMetric("尿常规", "尿微白蛋白", "", "g/L", 0.0, 0.15, MetricType.NUMERIC),
        HealthMetric("尿常规", "尿隐血", "", "cells/μl", type = MetricType.TEXT),
        HealthMetric("尿常规", "尿胆红素(UBIL)", "", "μmol/L", type = MetricType.TEXT),
        HealthMetric("尿常规", "尿胆原定性试验(UBG)", "", "μmol/L", type = MetricType.TEXT),
        HealthMetric("尿常规", "尿酮体定性试验(KETU)", "", "mmol/L", type = MetricType.TEXT),
        HealthMetric("尿常规", "尿蛋白定性试验(UPRO)", "", "g/L", type = MetricType.TEXT),
        HealthMetric("尿常规", "尿糖定性试验(UGLU)", "", "mmol/L", type = MetricType.TEXT),
        HealthMetric("尿常规", "亚硝酸盐(UNIT)", "", "-", type = MetricType.TEXT),
        HealthMetric("尿常规", "尿酸碱度检查(UPH)", "", "-", 5.4, 8.4, MetricType.NUMERIC),
        HealthMetric("尿常规", "尿比重测定(USG)", "", "-", 1.003, 1.030, MetricType.NUMERIC),
        HealthMetric("尿常规", "白细胞(ULEU)", "", "cells/μl", type = MetricType.TEXT),
        HealthMetric("尿常规", "维生素C(VC)", "", "mmol/L", type = MetricType.NUMERIC),
        HealthMetric("尿常规", "镜检红细胞", "", "个/HP", 0.0, 0.0, MetricType.NUMERIC),
        HealthMetric("尿常规", "镜检白细胞", "", "个/HP", 0.0, 0.0, MetricType.NUMERIC),
        HealthMetric("尿常规", "镜检其他", "", "-", type = MetricType.TEXT),
        HealthMetric("尿常规", "颜色", "", "-", type = MetricType.TEXT),
        HealthMetric("尿常规", "透明度", "", "-", type = MetricType.TEXT),


        // 2. 内科 - 全部设为空
        HealthMetric("内科", "神经系统", "", type = MetricType.TEXT),
        HealthMetric("内科", "腹部压痛", "", type = MetricType.TEXT),
        HealthMetric("内科", "腹部包块", "", type = MetricType.TEXT),
        HealthMetric("内科", "肠鸣音", "", type = MetricType.TEXT),
        HealthMetric("内科", "双肾", "", type = MetricType.TEXT),
        HealthMetric("内科", "脾", "", type = MetricType.TEXT),
        HealthMetric("内科", "肝", "", type = MetricType.TEXT),
        HealthMetric("内科", "肺部", "", type = MetricType.TEXT),
        HealthMetric("内科", "心脏杂音", "", type = MetricType.TEXT),
        HealthMetric("内科", "心律", "", type = MetricType.TEXT),
        HealthMetric("内科", "心界", "", type = MetricType.TEXT),
        HealthMetric("内科", "心率", "", "次/分", 60.0, 100.0, MetricType.NUMERIC),
        HealthMetric("内科", "药物过敏史", "", type = MetricType.TEXT),
        HealthMetric("内科", "传染病史", "", type = MetricType.TEXT),
        HealthMetric("内科", "家族史", "", type = MetricType.TEXT),
        HealthMetric("内科", "既往史", "", type = MetricType.TEXT),


        // 3. 腹部彩超
        HealthMetric("腹部彩超", "脾脏", "", type = MetricType.TEXT),
        HealthMetric("腹部彩超", "胰脏", "", type = MetricType.TEXT),
        HealthMetric("腹部彩超", "胆囊", "", type = MetricType.TEXT),
        HealthMetric("腹部彩超", "肝脏", "", type = MetricType.TEXT),



        // 6. 外科
        HealthMetric("外科", "四肢关节", "", type = MetricType.TEXT),
        HealthMetric("外科", "脊柱", "", type = MetricType.TEXT),
        HealthMetric("外科", "皮肤", "", type = MetricType.TEXT),
        HealthMetric("外科", "乳腺", "", type = MetricType.TEXT),
        HealthMetric("外科", "甲状腺", "", type = MetricType.TEXT),
        HealthMetric("外科", "淋巴结", "", type = MetricType.TEXT),
        HealthMetric("外科", "营养发育", "", type = MetricType.TEXT),
        HealthMetric("外科", "输血史", "", type = MetricType.TEXT),
        HealthMetric("外科", "病史及手术史", "", type = MetricType.TEXT),
        HealthMetric("外科", "肛门指检", "", type = MetricType.TEXT),

        // 7. 泌尿系统
        HealthMetric("泌尿系统", "膀胱", "", type = MetricType.TEXT),
        HealthMetric("泌尿系统", "输尿管(左、右)", "", type = MetricType.TEXT),
        HealthMetric("泌尿系统", "右肾", "", type = MetricType.TEXT),
        HealthMetric("泌尿系统", "左肾", "", type = MetricType.TEXT)
    )
}

// 定义报告类
data class HealthExamReport(
    val examName: String,     // 检查名称
    val findings: String = "", // 检查所见
    val conclusion: String = "" // 检查结论
)