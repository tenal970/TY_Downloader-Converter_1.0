package com.tenal.yaoitron.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenal.yaoitron.data.LogManager
import com.tenal.yaoitron.data.WallpaperManager
import com.tenal.yaoitron.engine.UpdateManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class UpdateViewModel : ViewModel() {

    // Library Updates State
    val ytDlpVersion: StateFlow<String> = UpdateManager.ytDlpVersion
    val ffmpegVersion: StateFlow<String> = UpdateManager.ffmpegVersion
    val isUpdating: StateFlow<Boolean> = UpdateManager.isUpdating
    val updateStatusMessage: StateFlow<String> = UpdateManager.statusMessage
    val logLines: StateFlow<List<String>> = LogManager.logLines

    // Wallpaper State
    val wallpaperFile: StateFlow<File?> = WallpaperManager.wallpaperFile

    fun changeWallpaper(context: Context, uri: Uri) {
        viewModelScope.launch {
            WallpaperManager.setWallpaper(context, uri)
        }
    }

    fun resetWallpaper(context: Context) {
        viewModelScope.launch {
            WallpaperManager.resetToDefault(context)
        }
    }

    fun refreshUpdates(context: Context) {
        viewModelScope.launch {
            UpdateManager.forceUpdate(context)
        }
    }
}
