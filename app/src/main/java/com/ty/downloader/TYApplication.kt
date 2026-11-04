package com.ty.downloader

import android.app.Application
import android.util.Log
import com.ty.downloader.data.WallpaperManager
import com.ty.downloader.engine.UpdateManager
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TYApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Move all heavy native binary setup, wallpaper loading, and update checks
        // off the main thread to completely prevent ANR (Application Not Responding)
        applicationScope.launch {
            setupBinaries()
            WallpaperManager.init(this@TYApplication)
            UpdateManager.checkAndUpdateOnStartup(this@TYApplication)
        }
    }

    /**
     * Initializes YoutubeDL and FFmpeg native binaries on Dispatchers.IO.
     */
    suspend fun setupBinaries() = withContext(Dispatchers.IO) {
        try {
            YoutubeDL.getInstance().init(this@TYApplication)
            Log.d(TAG, "YoutubeDL native binary initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize YoutubeDL: ${e.message}", e)
        }

        try {
            FFmpeg.getInstance().init(this@TYApplication)
            Log.d(TAG, "FFmpeg native binary initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FFmpeg: ${e.message}", e)
        }
    }

    companion object {
        const val TAG = "TYDownloader"
        lateinit var instance: TYApplication
            private set
    }
}
