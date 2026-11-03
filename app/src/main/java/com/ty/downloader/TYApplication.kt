package com.ty.downloader

import android.app.Application
import android.util.Log
import com.ty.downloader.engine.UpdateManager
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TYApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize YoutubeDL and FFmpeg native binaries
        try {
            YoutubeDL.getInstance().init(this)
            Log.d(TAG, "YoutubeDL initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize YoutubeDL: ${e.message}", e)
        }

        try {
            FFmpeg.getInstance().init(this)
            Log.d(TAG, "FFmpeg initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FFmpeg: ${e.message}", e)
        }

        // Initialize Wallpaper from storage
        com.ty.downloader.data.WallpaperManager.init(this)

        // Automatic library update on app startup (yt-dlp & ffmpeg check)
        applicationScope.launch {
            UpdateManager.checkAndUpdateOnStartup(this@TYApplication)
        }
    }

    companion object {
        const val TAG = "TYDownloader"
        lateinit var instance: TYApplication
            private set
    }
}
