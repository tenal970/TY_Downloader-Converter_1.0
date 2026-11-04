package com.tenal.yaoitron.ui.screens

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenal.yaoitron.data.DestinationFolder
import com.tenal.yaoitron.data.DownloadMode
import com.tenal.yaoitron.data.JobState
import com.tenal.yaoitron.data.QualityOption
import com.tenal.yaoitron.data.QualityPresets
import com.tenal.yaoitron.ui.components.CustomFolderDialog
import com.tenal.yaoitron.ui.components.PreviewCard
import com.tenal.yaoitron.ui.components.ProgressLogCard
import com.tenal.yaoitron.ui.theme.AmberWarning
import com.tenal.yaoitron.ui.theme.CardBorder
import com.tenal.yaoitron.ui.theme.CardSurface
import com.tenal.yaoitron.ui.theme.NeonViolet
import com.tenal.yaoitron.ui.theme.NeonVioletLight
import com.tenal.yaoitron.ui.theme.RubyRed
import com.tenal.yaoitron.ui.theme.TextMuted
import com.tenal.yaoitron.ui.theme.TextWhite

@Composable
fun DownloaderScreen(
    url: String,
    onUrlChange: (String) -> Unit,
    mode: DownloadMode,
    onModeChange: (DownloadMode) -> Unit,
    selectedQuality: QualityOption,
    onQualityChange: (QualityOption) -> Unit,
    destination: DestinationFolder,
    onDestinationChange: (DestinationFolder) -> Unit,
    customPath: String,
    onCustomPathChange: (String) -> Unit,
    jobState: JobState,
    logLines: List<String>,
    onCheckUrl: () -> Unit,
    onConfirmDownload: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showQualityDropdown by remember { mutableStateOf(false) }
    var showDestDropdown by remember { mutableStateOf(false) }
    var showCustomFolderDialog by remember { mutableStateOf(false) }

    val qualityList = if (mode == DownloadMode.VIDEO) {
        QualityPresets.VIDEO_QUALITIES
    } else {
        QualityPresets.AUDIO_QUALITIES
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Input Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Mode Switcher (Video vs Audio)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x44000000))
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        .padding(3.dp)
                ) {
                    ModeButton(
                        title = DownloadMode.VIDEO.label,
                        isSelected = mode == DownloadMode.VIDEO,
                        onClick = {
                            onModeChange(DownloadMode.VIDEO)
                            onQualityChange(QualityPresets.VIDEO_QUALITIES.first { it.id == "720" })
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ModeButton(
                        title = DownloadMode.AUDIO.label,
                        isSelected = mode == DownloadMode.AUDIO,
                        onClick = {
                            onModeChange(DownloadMode.AUDIO)
                            onQualityChange(QualityPresets.AUDIO_QUALITIES.first { it.id == "192" })
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // URL Input Field
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    placeholder = {
                        Text(
                            text = "วางลิงก์ (YouTube / X / Facebook / TikTok ...)",
                            color = TextMuted.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (url.isNotEmpty()) {
                                IconButton(onClick = { onUrlChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!clip.isNullOrBlank()) {
                                    onUrlChange(clip.trim())
                                }
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = NeonVioletLight)
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x33000000),
                        unfocusedContainerColor = Color(0x33000000),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedIndicatorColor = NeonViolet,
                        unfocusedIndicatorColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quality Dropdown selector
                Column {
                    Text(text = "ความละเอียด / คุณภาพ", color = TextMuted, fontSize = 12.sp)
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
                            Text(text = selectedQuality.title, color = TextWhite, fontSize = 14.sp)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown", tint = TextMuted)
                        }

                        DropdownMenu(
                            expanded = showQualityDropdown,
                            onDismissRequest = { showQualityDropdown = false },
                            modifier = Modifier.background(CardSurface)
                        ) {
                            qualityList.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.title, color = TextWhite) },
                                    onClick = {
                                        onQualityChange(option)
                                        showQualityDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Warning for 2K/4K/8K VP9 codec
                    if (selectedQuality.hint != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedQuality.hint!!,
                            color = AmberWarning,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Destination Folder Selector
                Column {
                    Text(text = "📁 บันทึกไฟล์ลงที่", color = TextMuted, fontSize = 12.sp)
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

                // Giant Action Button: "⬇ ดาวน์โหลด"
                val isBusy = jobState is JobState.Checking || jobState is JobState.Running
                Button(
                    onClick = {
                        if (!isBusy) {
                            onCheckUrl()
                        }
                    },
                    enabled = url.isNotBlank() && !isBusy,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(NeonViolet, Color(0xFF9E0059))
                            )
                        )
                ) {
                    Text(
                        text = if (jobState is JobState.Checking) "กำลังตรวจลิงก์..." else "⬇ ดาวน์โหลด",
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "ถ้าลิงก์เป็นเพลย์ลิสต์ จะโหลดแค่คลิปแรกคลิปเดียว",
                    color = TextMuted.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // Preview Card
        AnimatedVisibility(visible = jobState is JobState.PreviewReady) {
            if (jobState is JobState.PreviewReady) {
                PreviewCard(
                    info = jobState.info,
                    onConfirm = onConfirmDownload,
                    onCancel = onCancel
                )
            }
        }

        // Live Terminal Log & Progress Card
        AnimatedVisibility(visible = jobState !is JobState.Idle && jobState !is JobState.PreviewReady) {
            ProgressLogCard(
                state = jobState,
                logLines = logLines,
                onCancel = onCancel
            )
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

@Composable
private fun ModeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) NeonViolet else Color.Transparent
    val textColor = if (isSelected) TextWhite else TextMuted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
