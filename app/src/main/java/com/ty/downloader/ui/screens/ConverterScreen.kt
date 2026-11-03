package com.ty.downloader.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ty.downloader.data.ConvertQuality
import com.ty.downloader.data.DestinationFolder
import com.ty.downloader.data.GifSettings
import com.ty.downloader.data.JobState
import com.ty.downloader.data.MediaKind
import com.ty.downloader.data.SelectedFile
import com.ty.downloader.data.SupportedFormats
import com.ty.downloader.ui.components.CustomFolderDialog
import com.ty.downloader.ui.components.ProgressLogCard
import com.ty.downloader.ui.theme.CardBorder
import com.ty.downloader.ui.theme.CardSurface
import com.ty.downloader.ui.theme.EmeraldGreen
import com.ty.downloader.ui.theme.NeonViolet
import com.ty.downloader.ui.theme.NeonVioletLight
import com.ty.downloader.ui.theme.TextMuted
import com.ty.downloader.ui.theme.TextWhite

@Composable
fun ConverterScreen(
    selectedFile: SelectedFile?,
    onFileSelected: (SelectedFile) -> Unit,
    targetFormat: String,
    onTargetFormatChange: (String) -> Unit,
    quality: ConvertQuality,
    onQualityChange: (ConvertQuality) -> Unit,
    resizeWidth: String,
    onResizeWidthChange: (String) -> Unit,
    resizeHeight: String,
    onResizeHeightChange: (String) -> Unit,
    gifSettings: GifSettings,
    onGifSettingsChange: (GifSettings) -> Unit,
    destination: DestinationFolder,
    onDestinationChange: (DestinationFolder) -> Unit,
    customPath: String,
    onCustomPathChange: (String) -> Unit,
    jobState: JobState,
    logLines: List<String>,
    onStartConvert: () -> Unit,
    onCancelConvert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFormatDropdown by remember { mutableStateOf(false) }
    var showQualityDropdown by remember { mutableStateOf(false) }
    var showDestDropdown by remember { mutableStateOf(false) }
    var showCustomFolderDialog by remember { mutableStateOf(false) }

    // Launcher for file picker (Images, Audios, Videos)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "unknown_file"
            var fileSize = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            val mime = context.contentResolver.getType(uri)
            val kind = SupportedFormats.getKind(fileName, mime) ?: MediaKind.VIDEO
            val picked = SelectedFile(
                uriString = uri.toString(),
                name = fileName,
                sizeBytes = fileSize,
                kind = kind
            )
            onFileSelected(picked)

            // Set default format
            when (kind) {
                MediaKind.IMAGE -> onTargetFormatChange("png")
                MediaKind.AUDIO -> onTargetFormatChange("mp3")
                MediaKind.VIDEO -> onTargetFormatChange("mp4")
            }
        }
    }

    val availableFormats = if (selectedFile != null) {
        SupportedFormats.getTargetOptions(selectedFile.kind)
    } else {
        listOf("mp4", "mp3", "png", "webp", "gif")
    }

    val isExtractAudio = selectedFile?.kind == MediaKind.VIDEO && targetFormat in SupportedFormats.AUDIO_OUTPUTS
    val isGif = selectedFile?.kind == MediaKind.VIDEO && targetFormat == "gif"
    val showResize = selectedFile?.kind == MediaKind.IMAGE || (selectedFile?.kind == MediaKind.VIDEO && !isGif && !isExtractAudio)
    val showQuality = targetFormat !in listOf("png", "wav", "flac", "bmp", "tiff", "gif")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Config Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "🔄 แปลงไฟล์ภาพ / เสียง / วิดีโอ",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                // File Picker Button
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = "Pick File", tint = TextWhite)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (selectedFile != null) "เปลี่ยนไฟล์ที่เลือก" else "เลือกไฟล์จากมือถือ",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Selected File Info Card
                if (selectedFile != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33000000))
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = selectedFile.name,
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${selectedFile.kind.name} · ${selectedFile.formattedSize()}",
                                color = NeonVioletLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "เลือกไฟล์จาก Download, Gallery หรือ File Manager ของเครื่องได้",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Target Format Selector
                Column {
                    Text(text = "แปลงเป็นนามสกุล", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x33000000))
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                            .clickable { showFormatDropdown = true }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val label = targetFormat.uppercase() + if (isExtractAudio) " (ดึงเสียงออกมา)" else ""
                            Text(text = label, color = TextWhite, fontSize = 14.sp)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown", tint = TextMuted)
                        }

                        DropdownMenu(
                            expanded = showFormatDropdown,
                            onDismissRequest = { showFormatDropdown = false },
                            modifier = Modifier.background(CardSurface)
                        ) {
                            availableFormats.forEach { fmt ->
                                val fmtLabel = fmt.uppercase() + if (selectedFile?.kind == MediaKind.VIDEO && fmt in SupportedFormats.AUDIO_OUTPUTS) " (ดึงเสียง)" else ""
                                DropdownMenuItem(
                                    text = { Text(fmtLabel, color = TextWhite) },
                                    onClick = {
                                        onTargetFormatChange(fmt)
                                        showFormatDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Quality Selector (if applicable)
                if (showQuality) {
                    Column {
                        Text(text = "คุณภาพ", color = TextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x33000000))
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                                .clickable { showQualityDropdown = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = quality.label, color = TextWhite, fontSize = 14.sp)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown", tint = TextMuted)
                            }

                            DropdownMenu(
                                expanded = showQualityDropdown,
                                onDismissRequest = { showQualityDropdown = false },
                                modifier = Modifier.background(CardSurface)
                            ) {
                                ConvertQuality.values().forEach { q ->
                                    DropdownMenuItem(
                                        text = { Text(q.label, color = TextWhite) },
                                        onClick = {
                                            onQualityChange(q)
                                            showQualityDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Resize Settings (Optional)
                if (showResize) {
                    Column {
                        Text(text = "ปรับขนาด (เว้นว่างเพื่อใช้ขนาดเดิม)", color = TextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = resizeWidth,
                                onValueChange = onResizeWidthChange,
                                placeholder = { Text("ความกว้าง px", color = TextMuted.copy(0.5f), fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0x33000000),
                                    unfocusedContainerColor = Color(0x33000000),
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedIndicatorColor = NeonViolet,
                                    unfocusedIndicatorColor = CardBorder
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = resizeHeight,
                                onValueChange = onResizeHeightChange,
                                placeholder = { Text("ความสูง px", color = TextMuted.copy(0.5f), fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0x33000000),
                                    unfocusedContainerColor = Color(0x33000000),
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedIndicatorColor = NeonViolet,
                                    unfocusedIndicatorColor = CardBorder
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = "รักษาสัดส่วนภาพ ไม่ยืดภาพให้ผิดรูป",
                            color = TextMuted.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Video to GIF Settings
                if (isGif) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "ตั้งค่า GIF จากวิดีโอ", color = TextMuted, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = gifSettings.width.toString(),
                                onValueChange = { w ->
                                    w.toIntOrNull()?.let { onGifSettingsChange(gifSettings.copy(width = it)) }
                                },
                                label = { Text("กว้าง px", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = gifSettings.fps.toString(),
                                onValueChange = { fps ->
                                    fps.toIntOrNull()?.let { onGifSettingsChange(gifSettings.copy(fps = it)) }
                                },
                                label = { Text("FPS", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = gifSettings.startSeconds.toString(),
                                onValueChange = { s ->
                                    s.toFloatOrNull()?.let { onGifSettingsChange(gifSettings.copy(startSeconds = it)) }
                                },
                                label = { Text("เริ่มที่ (วิ)", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = gifSettings.durationSeconds.toString(),
                                onValueChange = { d ->
                                    d.toFloatOrNull()?.let { onGifSettingsChange(gifSettings.copy(durationSeconds = it)) }
                                },
                                label = { Text("ความยาว (สูงสุด 30วิ)", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = "GIF ใช้ 2-pass palettegen lanczos เพื่อภาพคมชัด ไม่แตก",
                            color = TextMuted.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Destination Folder Selector
                Column {
                    Text(text = "📁 บันทึกไฟล์ที่แปลงแล้วลงที่", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x33000000))
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                            .clickable { showDestDropdown = true }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val destTitle = if (destination == DestinationFolder.CUSTOM && customPath.isNotBlank()) {
                                "กำหนดเอง: $customPath"
                            } else {
                                destination.title
                            }
                            Text(text = destTitle, color = TextWhite, fontSize = 14.sp)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown", tint = TextMuted)
                        }

                        DropdownMenu(
                            expanded = showDestDropdown,
                            onDismissRequest = { showDestDropdown = false },
                            modifier = Modifier.background(CardSurface)
                        ) {
                            DestinationFolder.values().forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folder.title, color = TextWhite) },
                                    onClick = {
                                        onDestinationChange(folder)
                                        showDestDropdown = false
                                        if (folder == DestinationFolder.CUSTOM) {
                                            showCustomFolderDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Start Convert Button
                val isBusy = jobState is JobState.Running
                Button(
                    onClick = onStartConvert,
                    enabled = selectedFile != null && !isBusy,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isBusy) "กำลังแปลงไฟล์..." else "🔄 เริ่มแปลงไฟล์",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Live Log & Progress Card
        AnimatedVisibility(visible = jobState !is JobState.Idle) {
            ProgressLogCard(
                state = jobState,
                logLines = logLines,
                onCancel = onCancelConvert
            )
        }

        // Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "รูปแบบที่รองรับ", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "• ภาพ: JPG, PNG, WebP, BMP, TIFF และ GIF", color = TextMuted, fontSize = 12.sp)
                Text(text = "• เสียง: MP3, M4A, WAV, FLAC, OGG และ OPUS", color = TextMuted, fontSize = 12.sp)
                Text(text = "• วิดีโอ: MP4, MKV, WebM, MOV, AVI และ GIF", color = TextMuted, fontSize = 12.sp)
                Text(text = "• ดึงเสียงจากวิดีโอ: MP3, M4A, WAV, FLAC, OGG, OPUS", color = TextMuted, fontSize = 12.sp)
            }
        }
    }

    if (showCustomFolderDialog) {
        CustomFolderDialog(
            initialPath = customPath,
            onConfirm = {
                onCustomPathChange(it)
                showCustomFolderDialog = false
            },
            onDismiss = { showCustomFolderDialog = false }
        )
    }
}
