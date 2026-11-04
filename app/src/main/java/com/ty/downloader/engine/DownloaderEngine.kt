package com.ty.downloader.engine

import android.content.Context
import android.os.Environment
import android.util.Log
import com.ty.downloader.data.DownloadMode
import com.ty.downloader.data.LogManager
import com.ty.downloader.data.PreviewInfo
import com.ty.downloader.service.DownloadForegroundService
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.regex.Pattern

object DownloaderEngine {

    private const val TAG = "TYDownloaderEngine"
    private const val PROCESS_ID = "ty_download_task"
    private val TEMP_FILE_PATTERN = Pattern.compile("(\\.part|\\.ytdl|\\.temp)$|\\.f\\d+\\.[A-Za-z0-9]+(\\.part)?$")

    private var activeVideoId: String? = null
    private var activeOutDir: File? = null

    val defaultDownloadDir: File
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    /**
     * Inspects URL metadata and thumbnail using yt-dlp -J.
     */
    suspend fun fetchPreview(url: String): PreviewInfo {
        return withContext(Dispatchers.IO) {
            LogManager.startSession(url, "preview")
            LogManager.append("▶ ตรวจสอบลิงก์: ${LogManager.redactUrl(url)}")

            val request = YoutubeDLRequest(url).apply {
                addOption("-J")
                addOption("--no-playlist")
                addOption("--playlist-items", "1")
                addOption("-v")
                addOption("--no-colors")
            }

            try {
                val response = YoutubeDL.getInstance().execute(request) { _, _, line ->
                    if (!line.startsWith("[debug]")) {
                        LogManager.append(line)
                    }
                }

                val jsonStr = response.out
                val rootJson = JSONObject(jsonStr)

                // If playlist, take first entry
                val infoJson = if (rootJson.optString("_type") == "playlist") {
                    val entries = rootJson.optJSONArray("entries")
                    if (entries != null && entries.length() > 0) entries.getJSONObject(0) else rootJson
                } else {
                    rootJson
                }

                val title = infoJson.optString("title", "Unknown Title")
                val uploader = infoJson.optString("uploader", "")
                    .ifEmpty { infoJson.optString("channel", "") }
                    .ifEmpty { infoJson.optString("uploader_id", "Unknown Uploader") }
                val duration = infoJson.optLong("duration", 0L)
                val site = infoJson.optString("extractor_key", "")
                    .ifEmpty { infoJson.optString("extractor", "Web") }
                val id = infoJson.optString("id", "")
                val thumb = infoJson.optString("thumbnail", "")

                LogManager.append("✅ ตรวจลิงก์สำเร็จ: [$site] $title (id=$id, ${duration}s)")
                activeVideoId = id

                PreviewInfo(
                    ok = true,
                    title = title,
                    uploader = uploader,
                    durationSeconds = duration,
                    site = site,
                    id = id,
                    thumbUrl = thumb,
                    logPath = LogManager.currentLogFile?.absolutePath ?: ""
                )
            } catch (e: Exception) {
                val err = e.localizedMessage ?: e.message ?: "ดึงข้อมูลวิดีโอไม่ได้"
                LogManager.append("❌ เกิดข้อผิดพลาดในการตรวจลิงก์: $err")
                Log.e(TAG, "fetchPreview failed", e)
                PreviewInfo(
                    ok = false,
                    errorMessage = err,
                    logPath = LogManager.currentLogFile?.absolutePath ?: ""
                )
            }
        }
    }

    /**
     * Executes download of video or audio with precise flags matching ytdl_gui_v3_1.py.
     */
    suspend fun executeDownload(
        context: Context,
        url: String,
        mode: DownloadMode,
        quality: String,
        outDir: File = defaultDownloadDir,
        videoId: String? = null,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            val targetDir = if (outDir.exists() || outDir.mkdirs()) outDir else defaultDownloadDir.apply { mkdirs() }
            activeVideoId = videoId
            activeOutDir = targetDir

            LogManager.startSession(url, "download")
            LogManager.append("🚀 เริ่มดาวน์โหลด: ${LogManager.redactUrl(url)}")
            LogManager.append("📁 ปลายทาง: ${targetDir.absolutePath}")
            LogManager.append("⚙ โหมด: ${mode.name} | คุณภาพ: $quality")

            // Start foreground service to keep downloading alive in background
            try {
                DownloadForegroundService.startService(
                    context,
                    "กำลังดาวน์โหลดสื่อ",
                    "กำลังเชื่อมต่อไปยัง ${LogManager.redactUrl(url)}"
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start foreground service: ${e.message}")
            }

            val request = YoutubeDLRequest(url)

            if (mode == DownloadMode.AUDIO) {
                request.addOption("-x")
                request.addOption("--audio-format", "mp3")
                request.addOption("--audio-quality", "${quality}K")
                request.addOption("--convert-thumbnails", "jpg")
                request.addOption("--embed-thumbnail")
                request.addOption("--add-metadata")
            } else {
                val sort = when {
                    quality == "best" -> "vcodec:h264,res,acodec:m4a"
                    (quality.toIntOrNull() ?: 0) > 1080 -> "res:$quality,vcodec:vp9,acodec:m4a"
                    else -> "res:$quality,vcodec:h264,acodec:m4a"
                }
                request.addOption("-S", sort)
                request.addOption("--merge-output-format", "mp4")
            }

            request.addOption("--no-colors")
            request.addOption("--no-playlist")
            request.addOption("--playlist-items", "1")
            request.addOption("--newline")
            request.addOption("-v")
            request.addOption("--windows-filenames")
            request.addOption("-o", "${targetDir.absolutePath}/%(title).100B [%(id)s].%(ext)s")

            var downloadedFilePath: String? = null

            try {
                YoutubeDL.getInstance().execute(request, PROCESS_ID) { progress, etaInSeconds, line ->
                    if (!line.startsWith("[debug]")) {
                        LogManager.append(line)
                    }

                    // Look for completed destination path
                    if (line.contains("[download] Destination:") || line.contains("[Merger] Merging formats into")) {
                        val pathPart = line.substringAfter("Destination:").substringAfter("into").trim().trim('"')
                        if (pathPart.isNotEmpty() && File(pathPart).parentFile?.exists() == true) {
                            downloadedFilePath = pathPart
                        }
                    }

                    val etaText = if (etaInSeconds > 0) " (เหลือ ~${etaInSeconds}s)" else ""
                    val progressMsg = "กำลังดาวน์โหลด: ${"%.1f".format(progress)}%$etaText"
                    onProgress(progress, progressMsg)

                    // Update notification progress
                    DownloadForegroundService.updateProgress(
                        context,
                        "กำลังดาวน์โหลดสื่อ",
                        progressMsg,
                        progress.toInt()
                    )
                }

                // If path was not captured in stdout, find newest file matching id in targetDir
                if (downloadedFilePath == null && !videoId.isNullOrBlank()) {
                    val candidate = targetDir.listFiles { file ->
                        file.isFile && file.name.contains("[$videoId]") && !isTempFile(file.name)
                    }?.maxByOrNull { it.lastModified() }
                    downloadedFilePath = candidate?.absolutePath
                }

                val finalPath = downloadedFilePath ?: targetDir.absolutePath
                LogManager.append("🎉 ดาวน์โหลดเสร็จสมบูรณ์!")
                LogManager.append("📂 ไฟล์เซฟที่: $finalPath")

                // Media Scanner trigger (Native + termux-media-scan)
                if (downloadedFilePath != null) {
                    MediaScanner.scanFile(context, downloadedFilePath!!)
                }

                LogManager.finishSession("SUCCESS")
                Result.success(finalPath)
            } catch (e: Exception) {
                val err = e.localizedMessage ?: e.message ?: "การดาวน์โหลดล้มเหลว"
                LogManager.append("❌ ดาวน์โหลดไม่สำเร็จ: $err")
                LogManager.finishSession("FAILED: $err")
                Log.e(TAG, "Download execution failed", e)
                Result.failure(e)
            } finally {
                DownloadForegroundService.stopService(context)
                activeOutDir = null
            }
        }
    }

    /**
     * Cancels active download task and removes partial files.
     */
    suspend fun cancelDownload() {
        withContext(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().destroyProcessById(PROCESS_ID)
                LogManager.append("⏹ ยกเลิกการดาวน์โหลดโดยผู้ใช้")
            } catch (e: Exception) {
                Log.w(TAG, "Error destroying process: ${e.message}")
            }

            // Cleanup partials (.part, .ytdl, etc.)
            val outDir = activeOutDir
            val vidId = activeVideoId
            if (outDir != null && !vidId.isNullOrBlank() && outDir.exists()) {
                try {
                    val partialFiles = outDir.listFiles { file ->
                        file.name.contains("[$vidId]") && isTempFile(file.name)
                    }
                    partialFiles?.forEach { f ->
                        f.delete()
                        LogManager.append("🗑 ลบไฟล์ค้าง: ${f.name}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Partial cleanup error: ${e.message}")
                }
            }
            LogManager.finishSession("CANCELLED")
        }
    }

    private fun isTempFile(name: String): Boolean {
        return TEMP_FILE_PATTERN.matcher(name).find()
    }
}
