package com.tenal.yaoitron.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.tenal.yaoitron.data.MainTab
import com.tenal.yaoitron.ui.components.HeaderSection
import com.tenal.yaoitron.ui.components.TabBar
import com.tenal.yaoitron.ui.screens.ConverterScreen
import com.tenal.yaoitron.ui.screens.DownloaderScreen
import com.tenal.yaoitron.ui.theme.DarkMagentaBg
import com.tenal.yaoitron.ui.theme.DarkPurpleDeep
import com.tenal.yaoitron.ui.theme.YaoitronTheme
import com.tenal.yaoitron.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNecessaryPermissions()
        handleIncomingIntent(intent)

        setContent {
            YaoitronTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            viewModel.handleSharedText(sharedText)
        }
    }

    private fun requestNecessaryPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (_: Exception) {}
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    // Wallpaper picker launcher
    val wallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.changeWallpaper(context, uri)
        }
    }

    val wallpaperFile by viewModel.wallpaperFile.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val ytDlpVersion by viewModel.ytDlpVersion.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateStatusMessage by viewModel.updateStatusMessage.collectAsState()
    val logLines by viewModel.logLines.collectAsState()

    // Downloader states
    val url by viewModel.url.collectAsState()
    val mode by viewModel.downloadMode.collectAsState()
    val quality by viewModel.selectedQuality.collectAsState()
    val destination by viewModel.downloadDestination.collectAsState()
    val customPath by viewModel.downloadCustomPath.collectAsState()
    val downloadJobState by viewModel.downloadJobState.collectAsState()

    // Converter states
    val selectedFile by viewModel.selectedFile.collectAsState()
    val targetFormat by viewModel.targetFormat.collectAsState()
    val convertQuality by viewModel.convertQuality.collectAsState()
    val resizeWidth by viewModel.resizeWidth.collectAsState()
    val resizeHeight by viewModel.resizeHeight.collectAsState()
    val gifSettings by viewModel.gifSettings.collectAsState()
    val convertDestination by viewModel.convertDestination.collectAsState()
    val convertCustomPath by viewModel.convertCustomPath.collectAsState()
    val convertJobState by viewModel.convertJobState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Wallpaper Layer
        if (wallpaperFile != null && wallpaperFile!!.exists()) {
            AsyncImage(
                model = wallpaperFile,
                contentDescription = "Background Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Elegant dark gradient overlay to guarantee text legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xBB2A001B),
                                Color(0xDD140618)
                            )
                        )
                    )
            )
        } else {
            // Default luxurious gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkMagentaBg, DarkPurpleDeep)
                        )
                    )
            )
        }

        // Main UI Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // App Header with Version, Auto-update Status & Wallpaper Action
            HeaderSection(
                ytDlpVersion = ytDlpVersion,
                isUpdating = isUpdating,
                statusMessage = updateStatusMessage,
                hasCustomWallpaper = wallpaperFile != null && wallpaperFile!!.exists(),
                onRefreshUpdate = { viewModel.refreshUpdates(context) },
                onChangeWallpaper = { wallpaperLauncher.launch("image/*") },
                onResetWallpaper = { viewModel.resetWallpaper(context) }
            )

            // Main Tab Switcher
            TabBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.switchTab(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tab View Contents
            when (currentTab) {
                MainTab.DOWNLOADER -> {
                    DownloaderScreen(
                        url = url,
                        onUrlChange = { viewModel.setUrl(it) },
                        mode = mode,
                        onModeChange = { viewModel.setDownloadMode(it) },
                        selectedQuality = quality,
                        onQualityChange = { viewModel.setSelectedQuality(it) },
                        destination = destination,
                        onDestinationChange = { viewModel.setDownloadDestination(it) },
                        customPath = customPath,
                        onCustomPathChange = { viewModel.setDownloadCustomPath(it) },
                        jobState = downloadJobState,
                        logLines = logLines,
                        onCheckUrl = { viewModel.checkUrl() },
                        onConfirmDownload = { viewModel.confirmDownload(context) },
                        onCancel = { viewModel.cancelDownload() }
                    )
                }

                MainTab.CONVERTER -> {
                    ConverterScreen(
                        selectedFile = selectedFile,
                        onFileSelected = { viewModel.setSelectedFile(it) },
                        targetFormat = targetFormat,
                        onTargetFormatChange = { viewModel.setTargetFormat(it) },
                        quality = convertQuality,
                        onQualityChange = { viewModel.setConvertQuality(it) },
                        resizeWidth = resizeWidth,
                        onResizeWidthChange = { viewModel.setResizeWidth(it) },
                        resizeHeight = resizeHeight,
                        onResizeHeightChange = { viewModel.setResizeHeight(it) },
                        gifSettings = gifSettings,
                        onGifSettingsChange = { viewModel.setGifSettings(it) },
                        destination = convertDestination,
                        onDestinationChange = { viewModel.setConvertDestination(it) },
                        customPath = convertCustomPath,
                        onCustomPathChange = { viewModel.setConvertCustomPath(it) },
                        jobState = convertJobState,
                        logLines = logLines,
                        onStartConvert = { viewModel.startConvert(context) },
                        onCancelConvert = { viewModel.cancelConvert() }
                    )
                }
            }
        }
    }
}
