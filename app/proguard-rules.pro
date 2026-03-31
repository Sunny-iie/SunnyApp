# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 1. 保护 Room 数据库相关的生成的代码
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

-keep class androidx.room.** { *; }

# 2. 保护你的数据模型类（请确保包名 com.example.sunny 对应你的实际包名）
-keep class com.example.sunny.data.** { *; }

# 3. 保护 Gson 相关的转换逻辑（防止 Converters 报错）
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.Unsafe
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
# 保护你的泛型类型信息，这步对 List<HealthMetric> 非常关键
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type { *; }