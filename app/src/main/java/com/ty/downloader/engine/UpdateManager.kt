package com.ty.downloader.engine

import android.content.Context
import android.util.Log
import com.ty.downloader.data.LogManager
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

object UpdateManager {

    private const val TAG = "TYUpdateManager"

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _statusMessage = MutableStateFlow("พร้อมใช้งาน")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _ytDlpVersion = MutableStateFlow("กำลังโหลด...")
    val ytDlpVersion: StateFlow<String> = _ytDlpVersion.asStateFlow()

    private val _ffmpegVersion = MutableStateFlow("bundled")
    val ffmpegVersion: StateFlow<String> = _ffmpegVersion.asStateFlow()

    /**
     * Checks and updates yt-dlp to latest stable release and validates FFmpeg every time app opens.
     */
    suspend fun checkAndUpdateOnStartup(context: Context) {
        withContext(Dispatchers.IO) {
            _isUpdating.value = true
            _statusMessage.value = "🔄 กำลังตรวจสอบอัพเดท yt-dlp & ffmpeg..."
            Log.d(TAG, "Starting startup library update check...")

            // Read current yt-dlp version
            try {
                val currentVer = YoutubeDL.getInstance().version(context) ?: "unknown"
                _ytDlpVersion.value = "v$currentVer"
                LogManager.append("Current yt-dlp version: v$currentVer")
            } catch (e: Exception) {
                Log.w(TAG, "Cannot get yt-dlp version: ${e.message}")
            }

            // Perform yt-dlp update
            try {
                LogManager.append("🚀 กำลังเชื่อมต่อไปยัง GitHub เพื่ออัพเดท yt-dlp...")
                val result = YoutubeDL.getInstance().updateYoutubeDL(context, YoutubeDL.UpdateChannel._STABLE)
                
                val newVer = YoutubeDL.getInstance().version(context) ?: "unknown"
                _ytDlpVersion.value = "v$newVer"

                val msg = when (result) {
                    YoutubeDL.UpdateStatus.DONE -> "✅ อัพเดท yt-dlp เป็นเวอร์ชันใหม่แล้ว! (v$newVer)"
                    YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE -> "✅ yt-dlp เป็นเวอร์ชันล่าสุดแล้ว (v$newVer)"
                    else -> "✅ yt-dlp พร้อมใช้งาน (v$newVer)"
                }
                _statusMessage.value = msg
                LogManager.append(msg)
            } catch (e: Exception) {
                val errMsg = "⚠ ตรวจสอบอัพเดท yt-dlp ไม่สำเร็จ: ${e.localizedMessage ?: e.message}"
                _statusMessage.value = errMsg
                LogManager.append(errMsg)
                Log.e(TAG, "Failed to update yt-dlp", e)
            }

            // Ensure FFmpeg binary is initialized and ready
            try {
                FFmpeg.getInstance().init(context)
                _ffmpegVersion.value = "Active (FFmpeg 6.0/7.0)"
                LogManager.append("✅ FFmpeg พร้อมใช้งานสำหรับแปลงไฟล์และรวมเสียง")
            } catch (e: Exception) {
                _ffmpegVersion.value = "Error: ${e.message}"
                LogManager.append("⚠ FFmpeg init: ${e.message}")
            }

            _isUpdating.value = false
        }
    }

    suspend fun forceUpdate(context: Context) {
        checkAndUpdateOnStartup(context)
    }
}
