package com.ty.downloader.data

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object LogManager {

    private const val TAG = "TYLogManager"
    private const val MAX_IN_MEMORY_LINES = 200

    private val _logLines = MutableStateFlow<List<String>>(emptyList())
    val logLines: StateFlow<List<String>> = _logLines.asStateFlow()

    private var currentFileWriter: FileWriter? = null
    var currentLogFile: File? = null
        private set

    fun getLogDir(): File {
        val movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val logDir = File(movies, "TYLOG")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        return logDir
    }

    fun startSession(identifier: String, category: String = "dl") {
        clear()
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val safeSlug = identifier.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
            val logDir = getLogDir()
            val file = File(logDir, "ty_${category}_${timestamp}_${safeSlug}.log")
            currentLogFile = file
            currentFileWriter = FileWriter(file, true)

            append("==================================================")
            append("TY Downloader & Converter v3.1 Log")
            append("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            append("Target: ${redactText(identifier)}")
            append("Log File: ${file.absolutePath}")
            append("==================================================")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create log file: ${e.message}", e)
        }
    }

    fun append(line: String) {
        val sanitized = redactText(line)
        val current = _logLines.value.toMutableList()
        current.add(sanitized)
        if (current.size > MAX_IN_MEMORY_LINES) {
            _logLines.value = current.takeLast(MAX_IN_MEMORY_LINES)
        } else {
            _logLines.value = current
        }

        try {
            currentFileWriter?.apply {
                write(sanitized + "\n")
                flush()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed writing to log file: ${e.message}")
        }
    }

    fun clear() {
        try {
            currentFileWriter?.close()
        } catch (_: Exception) {}
        currentFileWriter = null
        currentLogFile = null
        _logLines.value = emptyList()
    }

    fun finishSession(summary: String) {
        append("--------------------------------------------------")
        append(summary)
        append("Session ended at: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        try {
            currentFileWriter?.close()
        } catch (_: Exception) {}
        currentFileWriter = null
    }

    private val URL_PATTERN = Pattern.compile("https?://[^\\s'\"]+")

    fun redactUrl(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val cleanUser = if (uri.userInfo != null) "<redacted>@" else ""
            "${uri.scheme}://$cleanUser${uri.host}${if (uri.port != -1) ":${uri.port}" else ""}${uri.path ?: ""}"
        } catch (_: Exception) {
            url
        }
    }

    fun redactText(text: String): String {
        val matcher = URL_PATTERN.matcher(text)
        val sb = StringBuffer()
        while (matcher.find()) {
            val matched = matcher.group()
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(redactUrl(matched)))
        }
        matcher.appendTail(sb)
        return sb.toString()
    }
}
