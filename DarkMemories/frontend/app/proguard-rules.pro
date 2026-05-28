# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod

# Keep model classes (ganti dengan package kamu)
-keep class com.sebassmith.darkmemories.model.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Generic type info untuk Retrofit
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep semua data class
-keepclassmembers class com.sebassmith.darkmemories.** {
    <fields>;
    <init>(...);
}