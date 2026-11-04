package com.tenal.yaoitron.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.tenal.yaoitron.data.LogManager
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

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
            _isUpdating.update { true }
            _statusMessage.update { "🔄 กำลังตรวจสอบอัพเดท yt-dlp & ffmpeg..." }
            Log.d(TAG, "Starting startup library update check...")

            // Read current yt-dlp version
            try {
                val currentVer = YoutubeDL.getInstance().version(context) ?: "unknown"
                _ytDlpVersion.update { "v$currentVer" }
                LogManager.append("Current yt-dlp version: v$currentVer")
            } catch (e: Exception) {
                Log.w(TAG, "Cannot get yt-dlp version: ${e.message}")
            }

            // Perform yt-dlp update
            try {
                LogManager.append("🚀 กำลังเชื่อมต่อไปยัง GitHub เพื่ออัพเดท yt-dlp...")
                val result = YoutubeDL.getInstance().updateYoutubeDL(context, YoutubeDL.UpdateChannel._STABLE)
                
                val newVer = YoutubeDL.getInstance().version(context) ?: "unknown"
                _ytDlpVersion.update { "v$newVer" }

                val msg = when (result) {
                    YoutubeDL.UpdateStatus.DONE -> "✅ อัพเดท yt-dlp เป็นเวอร์ชันใหม่แล้ว! (v$newVer)"
                    YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE -> "✅ yt-dlp เป็นเวอร์ชันล่าสุดแล้ว (v$newVer)"
                    else -> "✅ yt-dlp พร้อมใช้งาน (v$newVer)"
                }
                _statusMessage.update { msg }
                LogManager.append(msg)
            } catch (e: Exception) {
                val errMsg = "⚠ ตรวจสอบอัพเดท yt-dlp ไม่สำเร็จ: ${e.localizedMessage ?: e.message}"
                _statusMessage.update { errMsg }
                LogManager.append(errMsg)
                Log.e(TAG, "Failed to update yt-dlp", e)
            }

            // Ensure FFmpeg binary is initialized and ready
            try {
                FFmpeg.getInstance().init(context)
                _ffmpegVersion.update { "Active (FFmpeg 6.0/7.0)" }
                LogManager.append("✅ FFmpeg พร้อมใช้งานสำหรับแปลงไฟล์และรวมเสียง")
            } catch (e: Exception) {
                _ffmpegVersion.update { "Error: ${e.message}" }
                LogManager.append("⚠ FFmpeg init: ${e.message}")
            }

            _isUpdating.update { false }
        }
    }

    suspend fun forceUpdate(context: Context) {
        checkAndUpdateOnStartup(context)
    }

    /**
     * Calculates the SHA-256 checksum of a given file.
     */
    fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies that the SHA-256 checksum of the APK file matches expected checksum.
     */
    fun verifyApkChecksum(apkFile: File, expectedSha256: String): Boolean {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            LogManager.append("❌ ตรวจสอบ Checksum ล้มเหลว: ไม่พบไฟล์ APK")
            return false
        }
        val calculated = calculateSha256(apkFile)
        val matches = calculated.equals(expectedSha256.trim(), ignoreCase = true)
        if (matches) {
            LogManager.append("🛡️ การตรวจสอบ SHA-256 ผ่าน: $calculated")
        } else {
            LogManager.append("🚨 ตรวจพบความผิดปกติของไฟล์ APK (Checksum Mismatch)!")
            LogManager.append("   - คาดหวัง: ${expectedSha256.lowercase()}")
            LogManager.append("   - ที่ได้:   $calculated")
        }
        return matches
    }

    /**
     * Downloads an APK update file from a URL, verifies its SHA-256 checksum,
     * and deletes the file immediately if verification fails.
     */
    suspend fun downloadAndVerifyApk(
        context: Context,
        apkUrl: String,
        expectedSha256: String,
        onProgress: (Int) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val destFile = File(updatesDir, "update_${System.currentTimeMillis()}.apk")

        LogManager.append("⬇️ กำลังดาวน์โหลดอัปเดตแอป: ${LogManager.redactUrl(apkUrl)}")
        try {
            val url = URL(apkUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
            }

            val totalBytes = connection.contentLength
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                            onProgress(progress)
                        }
                    }
                }
            }

            // Security check: Validate SHA256 before allowing installation
            val isValid = verifyApkChecksum(destFile, expectedSha256)
            if (!isValid) {
                destFile.delete()
                val errorMsg = "ไฟล์ APK เสียหายหรือไม่ผ่านการตรวจสอบ SHA256 Checksum"
                Log.e(TAG, errorMsg)
                return@withContext Result.failure(SecurityException(errorMsg))
            }

            LogManager.append("✅ ดาวน์โหลดและยืนยันความปลอดภัยของ APK สำเร็จ")
            Result.success(destFile)
        } catch (e: Exception) {
            destFile.delete()
            Log.e(TAG, "Download APK failed: ${e.message}", e)
            LogManager.append("❌ เกิดข้อผิดพลาดในการดาวน์โหลด APK: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Launches the Android Package Installer for a verified APK file.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            val contentUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
            } else {
                Uri.fromFile(apkFile)
            }

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch APK installer: ${e.message}", e)
            LogManager.append("❌ ไม่สามารถเปิดตัวติดตั้ง APK ได้: ${e.message}")
        }
    }
}
