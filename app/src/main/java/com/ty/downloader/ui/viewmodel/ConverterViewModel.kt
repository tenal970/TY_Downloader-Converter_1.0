package com.ty.downloader.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ty.downloader.data.ConvertQuality
import com.ty.downloader.data.DestinationFolder
import com.ty.downloader.data.GifSettings
import com.ty.downloader.data.JobState
import com.ty.downloader.data.SelectedFile
import com.ty.downloader.engine.ConverterEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConverterViewModel : ViewModel() {

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

    fun setSelectedFile(file: SelectedFile) {
        _selectedFile.update { file }
    }

    fun setTargetFormat(format: String) {
        _targetFormat.update { format }
    }

    fun setConvertQuality(q: ConvertQuality) {
        _convertQuality.update { q }
    }

    fun setResizeWidth(w: String) {
        _resizeWidth.update { w }
    }

    fun setResizeHeight(h: String) {
        _resizeHeight.update { h }
    }

    fun setGifSettings(settings: GifSettings) {
        _gifSettings.update { settings }
    }

    fun setConvertDestination(dest: DestinationFolder) {
        _convertDestination.update { dest }
    }

    fun setConvertCustomPath(path: String) {
        _convertCustomPath.update { path }
    }

    fun startConvert(context: Context) {
        val file = _selectedFile.value ?: return

        _convertJobState.update { JobState.Running(0f, "กำลังเริ่มการแปลงไฟล์...") }

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
                _convertJobState.update { JobState.Running(progress, message) }
            }

            result.fold(
                onSuccess = { outPath ->
                    _convertJobState.update { JobState.Success("เซฟที่ $outPath", outPath) }
                },
                onFailure = { err ->
                    _convertJobState.update { JobState.Error(err.localizedMessage ?: "แปลงไฟล์ไม่สำเร็จ") }
                }
            )
        }
    }

    fun cancelConvert() {
        ConverterEngine.cancelConversion()
        _convertJobState.update { JobState.Cancelled }
    }
}
