package com.tenal.yaoitron.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenal.yaoitron.data.MainTab
import com.tenal.yaoitron.ui.theme.CardBorder
import com.tenal.yaoitron.ui.theme.NeonViolet
import com.tenal.yaoitron.ui.theme.TextMuted
import com.tenal.yaoitron.ui.theme.TextWhite

@Composable
fun TabBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x550A040E))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        TabButton(
            title = "⬇ Downloader",
            isSelected = currentTab == MainTab.DOWNLOADER,
            onClick = { onTabSelected(MainTab.DOWNLOADER) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            title = "🔄 File Converter",
            isSelected = currentTab == MainTab.CONVERTER,
            onClick = { onTabSelected(MainTab.CONVERTER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) NeonViolet else Color.Transparent
    val border = if (isSelected) Color(0xFFA28CFF) else Color.Transparent
    val textColor = if (isSelected) TextWhite else TextMuted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(9.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
