package com.ty.downloader.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object WallpaperManager {

    private const val TAG = "TYWallpaperManager"
    private const val WALLPAPER_FILE_NAME = "custom_app_wallpaper.jpg"

    private val _wallpaperFile = MutableStateFlow<File?>(null)
    val wallpaperFile: StateFlow<File?> = _wallpaperFile.asStateFlow()

    suspend fun init(context: Context) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, WALLPAPER_FILE_NAME)
        if (file.exists() && file.length() > 0) {
            _wallpaperFile.update { file }
        } else {
            _wallpaperFile.update { null }
        }
    }

    /**
     * Synchronous init fallback if needed during early initialization.
     */
    fun initSync(context: Context) {
        val file = File(context.filesDir, WALLPAPER_FILE_NAME)
        if (file.exists() && file.length() > 0) {
            _wallpaperFile.value = file
        } else {
            _wallpaperFile.value = null
        }
    }

    /**
     * Copies selected image from Gallery/Storage or downloads from HTTP/HTTPS URL into app internal storage
     * strictly on Dispatchers.IO to prevent NetworkOnMainThreadException or revoked URI grants.
     */
    suspend fun setWallpaper(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val destFile = File(context.filesDir, WALLPAPER_FILE_NAME)
                val scheme = uri.scheme?.lowercase()

                val inputStream = if (scheme == "http" || scheme == "https") {
                    val connection = (URL(uri.toString()).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 10000
                        readTimeout = 15000
                        instanceFollowRedirects = true
                    }
                    connection.inputStream
                } else {
                    context.contentResolver.openInputStream(uri)
                }

                inputStream?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (destFile.exists() && destFile.length() > 0) {
                    _wallpaperFile.update { destFile }
                    LogManager.append("🖼️ เปลี่ยนวอลเปเปอร์แอปสำเร็จ: ${destFile.length() / 1024} KB")
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save wallpaper: ${e.message}", e)
                LogManager.append("⚠ เปลี่ยนวอลเปเปอร์ไม่สำเร็จ: ${e.message}")
                false
            }
        }
    }

    /**
     * Downloads an image from a URL and sets it as the wallpaper on Dispatchers.IO.
     */
    suspend fun downloadWallpaperFromUrl(context: Context, imageUrl: String): Boolean {
        return setWallpaper(context, Uri.parse(imageUrl))
    }

    /**
     * Resets wallpaper to default dark magenta theme.
     */
    suspend fun resetToDefault(context: Context) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, WALLPAPER_FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
            _wallpaperFile.update { null }
            LogManager.append("🔄 คืนค่าวอลเปเปอร์เป็นแบบเริ่มต้นเรียบร้อย")
        }
    }
}
