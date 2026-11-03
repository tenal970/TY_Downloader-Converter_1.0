package com.ty.downloader.engine

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import com.ty.downloader.data.LogManager
import java.io.File

object MediaScanner {

    private const val TAG = "TYMediaScanner"

    /**
     * Scans newly downloaded or converted media file so it immediately appears in
     * the system Gallery, Google Photos, and Music players.
     * Also invokes termux-media-scan if running in a Termux environment or Termux:API is installed.
     */
    fun scanFile(context: Context, path: String) {
        val file = File(path)
        if (!file.exists()) {
            LogManager.append("Media scan skipped: file does not exist -> $path")
            return
        }

        LogManager.append("🔍 สแกน Media เข้าสู่ Gallery & ระบบ: ${file.name}")

        // 1. Android Native MediaScannerConnection
        try {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                null
            ) { scannedPath, uri ->
                Log.d(TAG, "MediaScannerConnection finished: $scannedPath -> $uri")
                LogManager.append("✅ Android MediaStore อัพเดทเรียบร้อย: $scannedPath")
            }
        } catch (e: Exception) {
            Log.w(TAG, "MediaScannerConnection error: ${e.message}")
        }

        // 2. Broadcast Intent
        try {
            val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = Uri.fromFile(file)
            }
            context.sendBroadcast(scanIntent)
        } catch (e: Exception) {
            Log.w(TAG, "Broadcast error: ${e.message}")
        }

        // 3. Termux-Media-Scan invocation (if accessible or Termux:API is installed)
        try {
            val termuxPaths = listOf(
                "/data/data/com.termux/files/usr/bin/termux-media-scan",
                "termux-media-scan"
            )
            for (binary in termuxPaths) {
                if (File(binary).canExecute() || binary == "termux-media-scan") {
                    val proc = ProcessBuilder(binary, file.absolutePath)
                        .redirectErrorStream(true)
                        .start()
                    val exit = proc.waitFor()
                    if (exit == 0) {
                        LogManager.append("✅ termux-media-scan ทำงานสำเร็จ: ${file.name}")
                        break
                    }
                }
            }
        } catch (_: Exception) {
            // Ignored if termux-media-scan is not installed on non-termux environments
        }
    }
}
