# Proguard rules for TY Downloader & Converter

-keep class com.yausername.youtubedl_android.** { *; }
-dontwarn com.yausername.youtubedl_android.**

-keep class com.yausername.ffmpeg.** { *; }
-dontwarn com.yausername.ffmpeg.**

# Keep Compose models and Data classes
-keep class com.ty.downloader.data.** { *; }
