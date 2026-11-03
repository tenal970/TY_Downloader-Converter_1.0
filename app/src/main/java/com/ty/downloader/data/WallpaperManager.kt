package com.ty.downloader.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object WallpaperManager {

    private const val TAG = "TYWallpaperManager"
    private const val WALLPAPER_FILE_NAME = "custom_app_wallpaper.jpg"

    private val _wallpaperFile = MutableStateFlow<File?>(null)
    val wallpaperFile: StateFlow<File?> = _wallpaperFile.asStateFlow()

    fun init(context: Context) {
        val file = File(context.filesDir, WALLPAPER_FILE_NAME)
        if (file.exists() && file.length() > 0) {
            _wallpaperFile.value = file
        } else {
            _wallpaperFile.value = null
        }
    }

    /**
     * Copies selected image from Gallery/Storage into app internal storage
     * to prevent Android SecurityException or revoked URI grants.
     */
    suspend fun setWallpaper(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val destFile = File(context.filesDir, WALLPAPER_FILE_NAME)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (destFile.exists() && destFile.length() > 0) {
                    _wallpaperFile.value = destFile
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
     * Resets wallpaper to default dark magenta theme.
     */
    suspend fun resetToDefault(context: Context) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, WALLPAPER_FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
            _wallpaperFile.value = null
            LogManager.append("🔄 คืนค่าวอลเปเปอร์เป็นแบบเริ่มต้นเรียบร้อย")
        }
    }
}
