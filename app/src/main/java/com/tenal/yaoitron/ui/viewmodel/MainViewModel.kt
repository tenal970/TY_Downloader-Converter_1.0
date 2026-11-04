package com.tenal.yaoitron.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.tenal.yaoitron.data.ConvertQuality
import com.tenal.yaoitron.data.DestinationFolder
import com.tenal.yaoitron.data.DownloadMode
import com.tenal.yaoitron.data.GifSettings
import com.tenal.yaoitron.data.JobState
import com.tenal.yaoitron.data.MainTab
import com.tenal.yaoitron.data.QualityOption
import com.tenal.yaoitron.data.SelectedFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

/**
 * Main coordinator ViewModel combining DownloaderViewModel, ConverterViewModel, and UpdateViewModel.
 */
class MainViewModel(
    val downloaderViewModel: DownloaderViewModel = DownloaderViewModel(),
    val converterViewModel: ConverterViewModel = ConverterViewModel(),
    val updateViewModel: UpdateViewModel = UpdateViewModel()
) : ViewModel() {

    // Tab Navigation
    private val _currentTab = MutableStateFlow(MainTab.DOWNLOADER)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    fun switchTab(tab: MainTab) {
        _currentTab.update { tab }
    }

    // Library Updates & Wallpaper (Delegated to UpdateViewModel)
    val ytDlpVersion: StateFlow<String> = updateViewModel.ytDlpVersion
    val ffmpegVersion: StateFlow<String> = updateViewModel.ffmpegVersion
    val isUpdating: StateFlow<Boolean> = updateViewModel.isUpdating
    val updateStatusMessage: StateFlow<String> = updateViewModel.updateStatusMessage
    val logLines: StateFlow<List<String>> = updateViewModel.logLines
    val wallpaperFile: StateFlow<File?> = updateViewModel.wallpaperFile

    fun changeWallpaper(context: Context, uri: Uri) = updateViewModel.changeWallpaper(context, uri)
    fun resetWallpaper(context: Context) = updateViewModel.resetWallpaper(context)
    fun refreshUpdates(context: Context) = updateViewModel.refreshUpdates(context)

    // Downloader Tab States (Delegated to DownloaderViewModel)
    val url: StateFlow<String> = downloaderViewModel.url
    val downloadMode: StateFlow<DownloadMode> = downloaderViewModel.downloadMode
    val selectedQuality: StateFlow<QualityOption> = downloaderViewModel.selectedQuality
    val downloadDestination: StateFlow<DestinationFolder> = downloaderViewModel.downloadDestination
    val downloadCustomPath: StateFlow<String> = downloaderViewModel.downloadCustomPath
    val downloadJobState: StateFlow<JobState> = downloaderViewModel.downloadJobState

    fun setUrl(newUrl: String) = downloaderViewModel.setUrl(newUrl)
    fun setDownloadMode(mode: DownloadMode) = downloaderViewModel.setDownloadMode(mode)
    fun setSelectedQuality(option: QualityOption) = downloaderViewModel.setSelectedQuality(option)
    fun setDownloadDestination(dest: DestinationFolder) = downloaderViewModel.setDownloadDestination(dest)
    fun setDownloadCustomPath(path: String) = downloaderViewModel.setDownloadCustomPath(path)
    fun checkUrl() = downloaderViewModel.checkUrl()
    fun confirmDownload(context: Context) = downloaderViewModel.confirmDownload(context)
    fun cancelDownload() = downloaderViewModel.cancelDownload()

    // Converter Tab States (Delegated to ConverterViewModel)
    val selectedFile: StateFlow<SelectedFile?> = converterViewModel.selectedFile
    val targetFormat: StateFlow<String> = converterViewModel.targetFormat
    val convertQuality: StateFlow<ConvertQuality> = converterViewModel.convertQuality
    val resizeWidth: StateFlow<String> = converterViewModel.resizeWidth
    val resizeHeight: StateFlow<String> = converterViewModel.resizeHeight
    val gifSettings: StateFlow<GifSettings> = converterViewModel.gifSettings
    val convertDestination: StateFlow<DestinationFolder> = converterViewModel.convertDestination
    val convertCustomPath: StateFlow<String> = converterViewModel.convertCustomPath
    val convertJobState: StateFlow<JobState> = converterViewModel.convertJobState

    fun setSelectedFile(file: SelectedFile) = converterViewModel.setSelectedFile(file)
    fun setTargetFormat(format: String) = converterViewModel.setTargetFormat(format)
    fun setConvertQuality(q: ConvertQuality) = converterViewModel.setConvertQuality(q)
    fun setResizeWidth(w: String) = converterViewModel.setResizeWidth(w)
    fun setResizeHeight(h: String) = converterViewModel.setResizeHeight(h)
    fun setGifSettings(settings: GifSettings) = converterViewModel.setGifSettings(settings)
    fun setConvertDestination(dest: DestinationFolder) = converterViewModel.setConvertDestination(dest)
    fun setConvertCustomPath(path: String) = converterViewModel.setConvertCustomPath(path)
    fun startConvert(context: Context) = converterViewModel.startConvert(context)
    fun cancelConvert() = converterViewModel.cancelConvert()

    // Intent handling (e.g. shared URL from YouTube/X/TikTok)
    fun handleSharedText(text: String?) {
        val handled = downloaderViewModel.handleSharedText(text)
        if (handled) {
            _currentTab.update { MainTab.DOWNLOADER }
        }
    }
}
