package com.ty.downloader.data

import android.os.Environment
import java.io.File

enum class MainTab {
    DOWNLOADER,
    CONVERTER
}

enum class DownloadMode(val label: String) {
    VIDEO("🎬 วิดีโอ MP4"),
    AUDIO("🎵 เสียง MP3")
}

data class QualityOption(
    val id: String,
    val title: String,
    val hint: String? = null
)

object QualityPresets {
    val VIDEO_QUALITIES = listOf(
        QualityOption("best", "ดีที่สุด (h264 สูงสุด 1080p)"),
        QualityOption("4320", "8K (4320p)", "⚠ 8K: ใช้ VP9/AV1 ไฟล์ใหญ่มาก รวมไฟล์นาน กินแบตและพื้นที่"),
        QualityOption("2160", "4K (2160p)", "⚠ 4K: ใช้ VP9/AV1 ไฟล์ใหญ่มาก รวมไฟล์นาน"),
        QualityOption("1440", "2K (1440p)", "⚠ 2K: ใช้ VP9 รวมไฟล์นาน"),
        QualityOption("1080", "1080p (แนะนำ)"),
        QualityOption("720", "720p"),
        QualityOption("480", "480p"),
        QualityOption("360", "360p")
    )

    val AUDIO_QUALITIES = listOf(
        QualityOption("320", "320 kbps (คุณภาพสูงสุด)"),
        QualityOption("192", "192 kbps (แนะนำ)"),
        QualityOption("128", "128 kbps (ประหยัดพื้นที่)")
    )
}

enum class DestinationFolder(val title: String) {
    DOWNLOADS("Download ของเครื่อง (แนะนำ)"),
    MOVIES("Movies ของเครื่อง"),
    MUSIC("Music ของเครื่อง"),
    PICTURES("Pictures ของเครื่อง"),
    CUSTOM("กำหนดโฟลเดอร์เอง");

    fun getDirectory(customPath: String? = null): File {
        return when (this) {
            DOWNLOADS -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            MOVIES -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            MUSIC -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            PICTURES -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            CUSTOM -> if (!customPath.isNullOrBlank()) File(customPath) else Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        }
    }
}

data class PreviewInfo(
    val ok: Boolean = false,
    val title: String = "",
    val uploader: String = "",
    val durationSeconds: Long = 0,
    val site: String = "",
    val id: String = "",
    val thumbUrl: String = "",
    val logPath: String = "",
    val errorMessage: String = ""
) {
    fun formattedDuration(): String {
        if (durationSeconds <= 0) return ""
        val h = durationSeconds / 3600
        val m = (durationSeconds % 3600) / 60
        val s = durationSeconds % 60
        val pad = { n: Long -> n.toString().padStart(2, '0') }
        return if (h > 0) "$h:${pad(m)}:${pad(s)}" else "$m:${pad(s)}"
    }
}

enum class ConvertQuality(val id: String, val label: String) {
    HIGH("high", "สูง (ไฟล์ใหญ่)"),
    MEDIUM("medium", "กลาง (แนะนำ)"),
    LOW("low", "ประหยัดพื้นที่")
}

enum class MediaKind {
    IMAGE,
    AUDIO,
    VIDEO
}

object SupportedFormats {
    val IMAGE_INPUTS = setOf("jpg", "jpeg", "png", "webp", "bmp", "tif", "tiff", "gif", "avif", "heic", "heif")
    val AUDIO_INPUTS = setOf("mp3", "m4a", "aac", "wav", "flac", "ogg", "opus", "wma", "aiff", "alac")
    val VIDEO_INPUTS = setOf("mp4", "m4v", "mkv", "webm", "mov", "avi", "wmv", "flv", "mpeg", "mpg", "3gp", "ts", "m2ts", "ogv", "gifv")

    val IMAGE_OUTPUTS = listOf("png", "jpg", "webp", "bmp", "tiff", "gif")
    val AUDIO_OUTPUTS = listOf("mp3", "m4a", "wav", "flac", "ogg", "opus")
    val VIDEO_OUTPUTS = listOf("mp4", "mkv", "webm", "mov", "avi", "gif")

    fun getKind(fileName: String, mimeType: String? = null): MediaKind? {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        if (ext in IMAGE_INPUTS || mimeType?.startsWith("image/") == true) return MediaKind.IMAGE
        if (ext in AUDIO_INPUTS || mimeType?.startsWith("audio/") == true) return MediaKind.AUDIO
        if (ext in VIDEO_INPUTS || mimeType?.startsWith("video/") == true) return MediaKind.VIDEO
        return null
    }

    fun getTargetOptions(kind: MediaKind): List<String> {
        return when (kind) {
            MediaKind.IMAGE -> IMAGE_OUTPUTS
            MediaKind.AUDIO -> AUDIO_OUTPUTS
            MediaKind.VIDEO -> VIDEO_OUTPUTS + AUDIO_OUTPUTS // Can also extract audio!
        }
    }
}

data class SelectedFile(
    val uriString: String,
    val name: String,
    val sizeBytes: Long,
    val kind: MediaKind,
    val localFilePath: String? = null
) {
    fun formattedSize(): String {
        val mib = sizeBytes / (1024.0 * 1024.0)
        return "%.2f MiB".format(mib)
    }
}

data class GifSettings(
    val width: Int = 480,
    val fps: Int = 12,
    val startSeconds: Float = 0f,
    val durationSeconds: Float = 10f
)

sealed class JobState {
    object Idle : JobState()
    object Checking : JobState()
    data class PreviewReady(val info: PreviewInfo) : JobState()
    data class Running(val progress: Float, val currentMessage: String) : JobState()
    data class Success(val message: String, val filePath: String? = null) : JobState()
    data class Error(val message: String) : JobState()
    object Cancelled : JobState()
}
