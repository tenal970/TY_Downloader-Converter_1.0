package com.ty.downloader.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ty.downloader.data.ConvertQuality
import com.ty.downloader.data.DestinationFolder
import com.ty.downloader.data.DownloadMode
import com.ty.downloader.data.GifSettings
import com.ty.downloader.data.JobState
import com.ty.downloader.data.LogManager
import com.ty.downloader.data.MainTab
import com.ty.downloader.data.QualityOption
import com.ty.downloader.data.QualityPresets
import com.ty.downloader.data.SelectedFile
import com.ty.downloader.engine.ConverterEngine
import com.ty.downloader.engine.DownloaderEngine
import com.ty.downloader.engine.UpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // Tab Navigation
    private val _currentTab = MutableStateFlow(MainTab.DOWNLOADER)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Library Updates
    val ytDlpVersion: StateFlow<String> = UpdateManager.ytDlpVersion
    val isUpdating: StateFlow<Boolean> = UpdateManager.isUpdating
    val updateStatusMessage: StateFlow<String> = UpdateManager.statusMessage
    val logLines: StateFlow<List<String>> = LogManager.logLines

    // Wallpaper
    val wallpaperFile: StateFlow<java.io.File?> = com.ty.downloader.data.WallpaperManager.wallpaperFile

    fun changeWallpaper(context: Context, uri: android.net.Uri) {
        viewModelScope.launch {
            com.ty.downloader.data.WallpaperManager.setWallpaper(context, uri)
        }
    }

    fun resetWallpaper(context: Context) {
        viewModelScope.launch {
            com.ty.downloader.data.WallpaperManager.resetToDefault(context)
        }
    }

    // Downloader Tab States
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

    // Converter Tab States
    private val _selectedFile = MutableStateFlow<SelectedFile?>(null)
    val selectedFile: StateFlow<SelectedFile?> = _selectedFile.asStateFlow()

    private val _targetFormat = MutableStateFlow("mp4")
    val targetFormat: StateFlow<String> = _targetFormat.asStateFlow()

    private val _convertQuality = MutableStateFlow(ConvertQuality.MEDIUM)
    val convertQuality: StateFlow<ConvertQuality> = _convertQuality.asStateFlow()

    private val _resizeWidth = MutableStateFlow("")
    val resizeWidth: StateFlow<String> = _resizeWidth.asStateFlow()

    private val _resizeHeight = MutableStateFlow("")
    val resizeHeight: StateFlow<String> = _resizeHeight.asStateFlow()

    private val _gifSettings = MutableStateFlow(GifSettings())
    val gifSettings: StateFlow<GifSettings> = _gifSettings.asStateFlow()

    private val _convertDestination = MutableStateFlow(DestinationFolder.DOWNLOADS)
    val convertDestination: StateFlow<DestinationFolder> = _convertDestination.asStateFlow()

    private val _convertCustomPath = MutableStateFlow("")
    val convertCustomPath: StateFlow<String> = _convertCustomPath.asStateFlow()

    private val _convertJobState = MutableStateFlow<JobState>(JobState.Idle)
    val convertJobState: StateFlow<JobState> = _convertJobState.asStateFlow()

    // Navigation
    fun switchTab(tab: MainTab) {
        _currentTab.value = tab
    }

    // Downloader Actions
    fun setUrl(newUrl: String) {
        _url.value = newUrl
        if (_downloadJobState.value is JobState.PreviewReady) {
            _downloadJobState.value = JobState.Idle
        }
    }

    fun setDownloadMode(mode: DownloadMode) {
        _downloadMode.value = mode
    }

    fun setSelectedQuality(option: QualityOption) {
        _selectedQuality.value = option
    }

    fun setDownloadDestination(dest: DestinationFolder) {
        _downloadDestination.value = dest
    }

    fun setDownloadCustomPath(path: String) {
        _downloadCustomPath.value = path
    }

    fun checkUrl() {
        val targetUrl = _url.value.trim()
        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
            _downloadJobState.value = JobState.Error("ลิงก์ต้องขึ้นต้นด้วย http:// หรือ https://")
            return
        }

        _downloadJobState.value = JobState.Checking
        viewModelScope.launch {
            val preview = DownloaderEngine.fetchPreview(targetUrl)
            if (preview.ok) {
                _downloadJobState.value = JobState.PreviewReady(preview)
            } else {
                _downloadJobState.value = JobState.Error(preview.errorMessage)
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

        _downloadJobState.value = JobState.Running(0f, "กำลังเริ่มต้นดาวน์โหลด...")

        viewModelScope.launch {
            val result = DownloaderEngine.executeDownload(
                context = context,
                url = targetUrl,
                mode = mode,
                quality = quality,
                outDir = outDir,
                videoId = preview?.id
            ) { progress, message ->
                _downloadJobState.value = JobState.Running(progress, message)
            }

            result.fold(
                onSuccess = { savedPath ->
                    _downloadJobState.value = JobState.Success("เซฟที่ $savedPath", savedPath)
                },
                onFailure = { error ->
                    _downloadJobState.value = JobState.Error(error.localizedMessage ?: "ดาวน์โหลดไม่สำเร็จ")
                }
            )
        }
    }

    fun cancelDownload() {
        viewModelScope.launch {
            DownloaderEngine.cancelDownload()
            _downloadJobState.value = JobState.Cancelled
        }
    }

    // Converter Actions
    fun setSelectedFile(file: SelectedFile) {
        _selectedFile.value = file
    }

    fun setTargetFormat(format: String) {
        _targetFormat.value = format
    }

    fun setConvertQuality(q: ConvertQuality) {
        _convertQuality.value = q
    }

    fun setResizeWidth(w: String) {
        _resizeWidth.value = w
    }

    fun setResizeHeight(h: String) {
        _resizeHeight.value = h
    }

    fun setGifSettings(settings: GifSettings) {
        _gifSettings.value = settings
    }

    fun setConvertDestination(dest: DestinationFolder) {
        _convertDestination.value = dest
    }

    fun setConvertCustomPath(path: String) {
        _convertCustomPath.value = path
    }

    fun startConvert(context: Context) {
        val file = _selectedFile.value ?: return

        _convertJobState.value = JobState.Running(0f, "กำลังเริ่มการแปลงไฟล์...")

        viewModelScope.launch {
            val width = _resizeWidth.value.toIntOrNull()
            val height = _resizeHeight.value.toIntOrNull()

            val result = ConverterEngine.convertFile(
                context = context,
                selectedFile = file,
                targetFormat = _targetFormat.value,
                quality = _convertQuality.value,
                width = width,
                height = height,
                gifSettings = _gifSettings.value,
                destination = _convertDestination.value,
                customPath = _convertCustomPath.value
            ) { progress, message ->
                _convertJobState.value = JobState.Running(progress, message)
            }

            result.fold(
                onSuccess = { outPath ->
                    _convertJobState.value = JobState.Success("เซฟที่ $outPath", outPath)
                },
                onFailure = { err ->
                    _convertJobState.value = JobState.Error(err.localizedMessage ?: "แปลงไฟล์ไม่สำเร็จ")
                }
            )
        }
    }

    fun cancelConvert() {
        ConverterEngine.cancelConversion()
        _convertJobState.value = JobState.Cancelled
    }

    // Refresh Library Updates
    fun refreshUpdates(context: Context) {
        viewModelScope.launch {
            UpdateManager.forceUpdate(context)
        }
    }

    // Intent handling (e.g. shared from YouTube/X app)
    fun handleSharedText(text: String?) {
        if (!text.isNullOrBlank()) {
            val extractedUrl = text.split("\\s+".toRegex()).firstOrNull {
                it.startsWith("http://") || it.startsWith("https://")
            }
            if (extractedUrl != null) {
                _url.value = extractedUrl
                _currentTab.value = MainTab.DOWNLOADER
                checkUrl()
            }
        }
    }
}
