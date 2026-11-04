package com.tenal.yaoitron.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenal.yaoitron.data.JobState
import com.tenal.yaoitron.ui.theme.AmberWarning
import com.tenal.yaoitron.ui.theme.CardBorder
import com.tenal.yaoitron.ui.theme.CardSurface
import com.tenal.yaoitron.ui.theme.EmeraldGreen
import com.tenal.yaoitron.ui.theme.NeonViolet
import com.tenal.yaoitron.ui.theme.RubyRed
import com.tenal.yaoitron.ui.theme.TerminalBg
import com.tenal.yaoitron.ui.theme.TextMuted
import com.tenal.yaoitron.ui.theme.TextWhite

@Composable
fun ProgressLogCard(
    state: JobState,
    logLines: List<String>,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state is JobState.Idle) return

    val listState = rememberLazyListState()
    LaunchedEffect(logLines.size) {
        if (logLines.isNotEmpty()) {
            listState.animateScrollToItem(logLines.size - 1)
        }
    }

    val borderColor = when (state) {
        is JobState.Success -> EmeraldGreen
        is JobState.Error -> RubyRed
        is JobState.Cancelled -> AmberWarning
        is JobState.Running, JobState.Checking, is JobState.PreviewReady -> NeonViolet
        else -> CardBorder
    }

    val progressValue = when (state) {
        is JobState.Running -> state.progress / 100f
        is JobState.Success -> 1f
        else -> 0f
    }
    val animatedProgress by animateFloatAsState(targetValue = progressValue, label = "progress")

    val message = when (state) {
        is JobState.Checking -> "กำลังตรวจสอบลิงก์..."
        is JobState.Running -> state.currentMessage
        is JobState.Success -> "เสร็จแล้ว ✅ ${state.message}"
        is JobState.Error -> "ไม่สำเร็จ ❌ ${state.message}"
        JobState.Cancelled -> "ยกเลิกแล้ว ⏹ (ลบไฟล์ที่ค้างให้แล้ว)"
        else -> ""
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardSurface)
            .border(2.dp, borderColor.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Status text
            Text(
                text = message,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = NeonViolet,
                trackColor = Color(0x33FFFFFF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Terminal log box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalBg)
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(state = listState) {
                    items(logLines) { line ->
                        Text(
                            text = line,
                            color = when {
                                line.startsWith("ERROR") || line.contains("❌") -> RubyRed
                                line.startsWith("WARNING") || line.contains("⚠") -> AmberWarning
                                line.contains("✅") || line.contains("🎉") -> EmeraldGreen
                                else -> TextMuted
                            },
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Cancel button when running
            if (state is JobState.Running || state is JobState.Checking) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = RubyRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⏹ ยกเลิก",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
