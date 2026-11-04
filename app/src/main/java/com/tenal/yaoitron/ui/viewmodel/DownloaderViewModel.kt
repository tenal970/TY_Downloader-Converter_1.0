package com.tenal.yaoitron.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenal.yaoitron.data.DestinationFolder
import com.tenal.yaoitron.data.DownloadMode
import com.tenal.yaoitron.data.JobState
import com.tenal.yaoitron.data.QualityOption
import com.tenal.yaoitron.data.QualityPresets
import com.tenal.yaoitron.engine.DownloaderEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DownloaderViewModel : ViewModel() {

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _downloadMode = MutableStateFlow(DownloadMode.VIDEO)
    val downloadMode: StateFlow<DownloadMode> = _downloadMode.asStateFlow()

    private val _selectedQuality = MutableStateFlow(QualityPresets.VIDEO_QUALITIES.first { it.id == "720" })
    val selectedQuality: StateFlow<QualityOption> = _selectedQuality.asStateFlow()

    private val _downloadDestination = MutableStateFlow(DestinationFolder.DOWNLOADS)
    val downloadDestination: StateFlow<DestinationFolder> = _downloadDestination.asStateFlow()

    private val _downloadCustomPath = MutableStateFlow("")
    val downloadCustomPath: StateFlow<String> = _downloadCustomPath.asStateFlow()

    private val _downloadJobState = MutableStateFlow<JobState>(JobState.Idle)
    val downloadJobState: StateFlow<JobState> = _downloadJobState.asStateFlow()

    fun setUrl(newUrl: String) {
        _url.update { newUrl }
        if (_downloadJobState.value is JobState.PreviewReady) {
            _downloadJobState.update { JobState.Idle }
        }
    }

    fun setDownloadMode(mode: DownloadMode) {
        _downloadMode.update { mode }
    }

    fun setSelectedQuality(option: QualityOption) {
        _selectedQuality.update { option }
    }

    fun setDownloadDestination(dest: DestinationFolder) {
        _downloadDestination.update { dest }
    }

    fun setDownloadCustomPath(path: String) {
        _downloadCustomPath.update { path }
    }

    fun checkUrl() {
        val targetUrl = _url.value.trim()
        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
            _downloadJobState.update { JobState.Error("ลิงก์ต้องขึ้นต้นด้วย http:// หรือ https://") }
            return
        }

        _downloadJobState.update { JobState.Checking }
        viewModelScope.launch {
            val preview = DownloaderEngine.fetchPreview(targetUrl)
            if (preview.ok) {
                _downloadJobState.update { JobState.PreviewReady(preview) }
            } else {
                _downloadJobState.update { JobState.Error(preview.errorMessage) }
            }
        }
    }

    fun confirmDownload(context: Context) {
        val currentState = _downloadJobState.value
        val preview = if (currentState is JobState.PreviewReady) currentState.info else null

        val targetUrl = _url.value.trim()
        val mode = _downloadMode.value
        val quality = _selectedQuality.value.id
        val outDir = _downloadDestination.value.getDirectory(_downloadCustomPath.value)

        _downloadJobState.update { JobState.Running(0f, "กำลังเริ่มต้นดาวน์โหลด...") }

        viewModelScope.launch {
            val result = DownloaderEngine.executeDownload(
                context = context,
                url = targetUrl,
                mode = mode,
                quality = quality,
                outDir = outDir,
                videoId = preview?.id
            ) { progress, message ->
                _downloadJobState.update { JobState.Running(progress, message) }
            }

            result.fold(
                onSuccess = { savedPath ->
                    _downloadJobState.update { JobState.Success("เซฟที่ $savedPath", savedPath) }
                },
                onFailure = { error ->
                    _downloadJobState.update { JobState.Error(error.localizedMessage ?: "ดาวน์โหลดไม่สำเร็จ") }
                }
            )
        }
    }

    fun cancelDownload() {
        viewModelScope.launch {
            DownloaderEngine.cancelDownload()
            _downloadJobState.update { JobState.Cancelled }
        }
    }

    fun handleSharedText(text: String?): Boolean {
        if (!text.isNullOrBlank()) {
            val extractedUrl = text.split("\\s+".toRegex()).firstOrNull {
                it.startsWith("http://") || it.startsWith("https://")
            }
            if (extractedUrl != null) {
                _url.update { extractedUrl }
                checkUrl()
                return true
            }
        }
        return false
    }
}
