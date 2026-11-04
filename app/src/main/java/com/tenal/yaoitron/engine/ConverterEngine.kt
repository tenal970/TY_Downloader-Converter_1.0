package com.tenal.yaoitron.engine

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tenal.yaoitron.data.ConvertQuality
import com.tenal.yaoitron.data.DestinationFolder
import com.tenal.yaoitron.data.GifSettings
import com.tenal.yaoitron.data.LogManager
import com.tenal.yaoitron.data.MediaKind
import com.tenal.yaoitron.data.SelectedFile
import com.tenal.yaoitron.data.SupportedFormats
import com.tenal.yaoitron.service.DownloadForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.regex.Pattern

object ConverterEngine {

    private const val TAG = "TYConverterEngine"
    private var activeProcess: Process? = null
    private var activeOutputFile: File? = null

    /**
     * Resolves the usable FFmpeg executable bundled with youtubedl-android or system.
     */
    fun findFfmpegBinary(context: Context): String? {
        val candidates = listOf(
            File(context.filesDir, "packages/ffmpeg/usr/bin/ffmpeg"),
            File(context.noBackupFilesDir, "youtubedl-android/packages/ffmpeg/usr/bin/ffmpeg"),
            File("/data/data/com.termux/files/usr/bin/ffmpeg"),
            File("/system/bin/ffmpeg")
        )

        for (candidate in candidates) {
            if (candidate.exists() && candidate.canExecute()) {
                return candidate.absolutePath
            }
        }
        return "ffmpeg"
    }

    /**
     * Copies content uri from system file picker to an accessible local cache file.
     */
    fun prepareSourceFile(context: Context, uri: Uri, fileName: String): File {
        val cacheDir = File(context.cacheDir, "converter_input")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val tempFile = File(cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    /**
     * Builds safe unique destination file path avoiding overwriting.
     */
    fun createUniqueOutputFile(outDir: File, baseName: String, targetExtension: String): File {
        val cleanStem = baseName.substringBeforeLast('.').replace(Regex("[^a-zA-Z0-9._-]"), "_").take(60)
        var targetFile = File(outDir, "$cleanStem.$targetExtension")
        var counter = 1
        while (targetFile.exists()) {
            targetFile = File(outDir, "${cleanStem}_$counter.$targetExtension")
            counter++
        }
        return targetFile
    }

    private fun buildDimensionFilter(width: Int?, height: Int?): String? {
        val w = width ?: 0
        val h = height ?: 0
        if (w <= 0 && h <= 0) return null
        return when {
            w > 0 && h > 0 -> "scale=$w:$h:force_original_aspect_ratio=decrease:force_divisible_by=2"
            w > 0 -> "scale=$w:-2:force_divisible_by=2"
            h > 0 -> "scale=-2:$h:force_divisible_by=2"
            else -> null
        }
    }

    /**
     * Builds full FFmpeg command list faithful to ytdl_gui_v3_1.py.
     */
    fun buildFfmpegCommand(
        ffmpegPath: String,
        sourceFile: File,
        destinationFile: File,
        kind: MediaKind,
        targetFormat: String,
        quality: ConvertQuality,
        width: Int?,
        height: Int?,
        gifSettings: GifSettings
    ): List<String> {
        val target = targetFormat.lowercase().removePrefix(".")
        val cmd = mutableListOf(ffmpegPath, "-hide_banner", "-y", "-i", sourceFile.absolutePath)

        var effectiveKind = kind
        if (kind == MediaKind.VIDEO && target in SupportedFormats.AUDIO_OUTPUTS) {
            effectiveKind = MediaKind.AUDIO // Extract audio from video
        }

        when (effectiveKind) {
            MediaKind.IMAGE -> {
                val vf = buildDimensionFilter(width, height)
                if (vf != null) {
                    cmd.addAll(listOf("-vf", vf))
                }
                cmd.addAll(listOf("-frames:v", "1"))
                when (target) {
                    "jpg", "jpeg" -> {
                        val qv = when (quality) {
                            ConvertQuality.HIGH -> "1"
                            ConvertQuality.MEDIUM -> "2"
                            ConvertQuality.LOW -> "6"
                        }
                        cmd.addAll(listOf("-q:v", qv))
                        if (quality == ConvertQuality.HIGH) {
                            cmd.addAll(listOf("-qmin", "1"))
                        }
                    }
                    "webp" -> {
                        val qv = when (quality) {
                            ConvertQuality.HIGH -> "95"
                            ConvertQuality.MEDIUM -> "85"
                            ConvertQuality.LOW -> "65"
                        }
                        cmd.addAll(listOf("-quality", qv))
                    }
                    "gif" -> {
                        cmd.addAll(listOf("-loop", "0"))
                    }
                }
            }

            MediaKind.AUDIO -> {
                val abr = when (quality) {
                    ConvertQuality.HIGH -> "320k"
                    ConvertQuality.MEDIUM -> "192k"
                    ConvertQuality.LOW -> "128k"
                }
                val opusAbr = when (quality) {
                    ConvertQuality.HIGH -> "192k"
                    ConvertQuality.MEDIUM -> "128k"
                    ConvertQuality.LOW -> "96k"
                }
                val oggQ = when (quality) {
                    ConvertQuality.HIGH -> "8"
                    ConvertQuality.MEDIUM -> "5"
                    ConvertQuality.LOW -> "3"
                }

                cmd.add("-vn")
                when (target) {
                    "mp3" -> cmd.addAll(listOf("-c:a", "libmp3lame", "-b:a", abr))
                    "m4a" -> cmd.addAll(listOf("-c:a", "aac", "-b:a", abr))
                    "wav" -> cmd.addAll(listOf("-c:a", "pcm_s16le"))
                    "flac" -> cmd.addAll(listOf("-c:a", "flac"))
                    "ogg" -> cmd.addAll(listOf("-c:a", "libvorbis", "-q:a", oggQ))
                    "opus" -> cmd.addAll(listOf("-c:a", "libopus", "-b:a", opusAbr))
                    else -> cmd.addAll(listOf("-c:a", "libmp3lame", "-b:a", abr))
                }
            }

            MediaKind.VIDEO -> {
                if (target == "gif") {
                    val gWidth = gifSettings.width.coerceIn(64, 1280)
                    val gFps = gifSettings.fps.coerceIn(1, 20)
                    val gStart = gifSettings.startSeconds.coerceAtLeast(0f)
                    val gDur = gifSettings.durationSeconds.coerceIn(0.1f, 30f)

                    val gifFilter = "fps=$gFps,scale=$gWidth:-1:flags=lanczos,split[a][b];" +
                            "[a]palettegen=stats_mode=diff[p];[b][p]paletteuse=dither=bayer"

                    return listOf(
                        ffmpegPath, "-hide_banner", "-y",
                        "-ss", gStart.toString(),
                        "-i", sourceFile.absolutePath,
                        "-t", gDur.toString(),
                        "-filter_complex_threads", "1",
                        "-filter_complex", gifFilter,
                        "-loop", "0",
                        "-progress", "pipe:1",
                        "-nostats",
                        "-loglevel", "warning",
                        destinationFile.absolutePath
                    )
                }

                val crf = when (quality) {
                    ConvertQuality.HIGH -> "18"
                    ConvertQuality.MEDIUM -> "23"
                    ConvertQuality.LOW -> "28"
                }
                val vp9Crf = when (quality) {
                    ConvertQuality.HIGH -> "27"
                    ConvertQuality.MEDIUM -> "32"
                    ConvertQuality.LOW -> "38"
                }
                val mpeg4Q = when (quality) {
                    ConvertQuality.HIGH -> "3"
                    ConvertQuality.MEDIUM -> "5"
                    ConvertQuality.LOW -> "9"
                }
                val vab = when (quality) {
                    ConvertQuality.HIGH -> "256k"
                    ConvertQuality.MEDIUM -> "160k"
                    ConvertQuality.LOW -> "128k"
                }

                val x264 = listOf("-c:v", "libx264", "-preset", "veryfast", "-crf", crf, "-c:a", "aac", "-b:a", vab)

                val vf = buildDimensionFilter(width, height)
                if (vf != null) {
                    cmd.addAll(listOf("-vf", vf))
                }

                cmd.addAll(listOf("-threads", "2"))
                when (target) {
                    "mp4" -> cmd.addAll(x264 + listOf("-movflags", "+faststart"))
                    "mkv" -> cmd.addAll(x264)
                    "mov" -> cmd.addAll(x264 + listOf("-movflags", "+faststart"))
                    "webm" -> cmd.addAll(listOf("-c:v", "libvpx-vp9", "-deadline", "realtime", "-cpu-used", "5", "-crf", vp9Crf, "-b:v", "0", "-c:a", "libopus", "-b:a", "128k"))
                    "avi" -> cmd.addAll(listOf("-c:v", "mpeg4", "-q:v", mpeg4Q, "-c:a", "libmp3lame", "-b:a", vab))
                    else -> cmd.addAll(x264)
                }
            }
        }

        cmd.addAll(listOf("-progress", "pipe:1", "-nostats", "-loglevel", "warning", destinationFile.absolutePath))
        return cmd
    }

    /**
     * Executes the conversion process asynchronously.
     */
    suspend fun convertFile(
        context: Context,
        selectedFile: SelectedFile,
        targetFormat: String,
        quality: ConvertQuality,
        width: Int?,
        height: Int?,
        gifSettings: GifSettings,
        destination: DestinationFolder,
        customPath: String?,
        onProgress: (Float, String) -> Unit
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            LogManager.startSession(selectedFile.name, "convert")
            LogManager.append("🔄 เริ่มการแปลงไฟล์: ${selectedFile.name}")

            val outDir = destination.getDirectory(customPath)
            if (!outDir.exists()) outDir.mkdirs()

            val outputFile = createUniqueOutputFile(outDir, selectedFile.name, targetFormat)
            activeOutputFile = outputFile

            val sourceFile = prepareSourceFile(context, Uri.parse(selectedFile.uriString), selectedFile.name)
            val ffmpegBinary = findFfmpegBinary(context) ?: "ffmpeg"

            val cmd = buildFfmpegCommand(
                ffmpegPath = ffmpegBinary,
                sourceFile = sourceFile,
                destinationFile = outputFile,
                kind = selectedFile.kind,
                targetFormat = targetFormat,
                quality = quality,
                width = width,
                height = height,
                gifSettings = gifSettings
            )

            LogManager.append("⚙ คำสั่ง FFmpeg: ${cmd.joinToString(" ")}")
            LogManager.append("📁 ปลายทาง: ${outputFile.absolutePath}")

            try {
                DownloadForegroundService.startService(
                    context,
                    "กำลังแปลงไฟล์",
                    "กำลังแปลง ${selectedFile.name} เป็น $targetFormat"
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start foreground service: ${e.message}")
            }

            try {
                val pb = ProcessBuilder(cmd)
                pb.redirectErrorStream(true)
                val process = pb.start()
                activeProcess = process

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                val timePattern = Pattern.compile("out_time_ms=(\\d+)")

                while (reader.readLine().also { line = it } != null) {
                    val l = line ?: continue
                    if (l.isNotBlank()) {
                        LogManager.append(l)

                        val matcher = timePattern.matcher(l)
                        if (matcher.find()) {
                            val timeMs = matcher.group(1)?.toLongOrNull() ?: 0L
                            val sec = timeMs / 1000000.0
                            onProgress(50f, "กำลังประมวลผล: %.1fs".format(sec))
                        }
                    }
                }

                val exitCode = process.waitFor()
                if (exitCode == 0 && outputFile.exists()) {
                    LogManager.append("🎉 แปลงไฟล์สำเร็จเรียบร้อย!")
                    LogManager.append("📂 เซฟที่: ${outputFile.absolutePath}")

                    // Scan file into Android Gallery & Music Player
                    MediaScanner.scanFile(context, outputFile.absolutePath)
                    LogManager.finishSession("SUCCESS")
                    Result.success(outputFile.absolutePath)
                } else {
                    val errMsg = "FFmpeg จบด้วยข้อผิดพลาด (Exit code: $exitCode)"
                    LogManager.append("❌ $errMsg")
                    LogManager.finishSession("FAILED: $errMsg")
                    Result.failure(Exception(errMsg))
                }
            } catch (e: Exception) {
                val errMsg = e.localizedMessage ?: e.message ?: "การแปลงไฟล์ล้มเหลว"
                LogManager.append("❌ เกิดข้อผิดพลาด: $errMsg")
                LogManager.finishSession("FAILED: $errMsg")
                Result.failure(e)
            } finally {
                DownloadForegroundService.stopService(context)
                activeProcess = null
                activeOutputFile = null
                try { sourceFile.delete() } catch (_: Exception) {}
            }
        }
    }

    fun cancelConversion() {
        try {
            activeProcess?.destroy()
            activeProcess = null
            activeOutputFile?.delete()
            activeOutputFile = null
            LogManager.append("⏹ ยกเลิกการแปลงไฟล์แล้ว")
            LogManager.finishSession("CANCELLED")
        } catch (e: Exception) {
            Log.w(TAG, "Cancel error: ${e.message}")
        }
    }
}
