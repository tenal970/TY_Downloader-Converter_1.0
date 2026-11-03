package com.ty.downloader.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ty.downloader.ui.theme.CardBorder
import com.ty.downloader.ui.theme.CardSurface
import com.ty.downloader.ui.theme.EmeraldGreen
import com.ty.downloader.ui.theme.NeonViolet
import com.ty.downloader.ui.theme.NeonVioletLight
import com.ty.downloader.ui.theme.RubyRed
import com.ty.downloader.ui.theme.TextMuted
import com.ty.downloader.ui.theme.TextWhite

@Composable
fun HeaderSection(
    ytDlpVersion: String,
    isUpdating: Boolean,
    statusMessage: String,
    hasCustomWallpaper: Boolean,
    onRefreshUpdate: () -> Unit,
    onChangeWallpaper: () -> Unit,
    onResetWallpaper: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    var showWallpaperMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title + Version Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⬇️ TY Downloader",
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(NeonViolet.copy(alpha = 0.35f))
                        .border(1.dp, NeonVioletLight.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "v3.1",
                        color = NeonVioletLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Action Buttons: Wallpaper Picker & Update Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Wallpaper Button & Dropdown Menu
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (hasCustomWallpaper) NeonViolet.copy(alpha = 0.4f) else Color(0x33000000))
                            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                            .clickable { showWallpaperMenu = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Wallpaper",
                            tint = if (hasCustomWallpaper) NeonVioletLight else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "วอลเปเปอร์",
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    DropdownMenu(
                        expanded = showWallpaperMenu,
                        onDismissRequest = { showWallpaperMenu = false },
                        modifier = Modifier.background(CardSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("🖼️ เลือกรูปใหม่จาก Gallery / Storage", color = TextWhite, fontSize = 13.sp) },
                            onClick = {
                                showWallpaperMenu = false
                                onChangeWallpaper()
                            }
                        )
                        if (hasCustomWallpaper) {
                            DropdownMenuItem(
                                text = { Text("🔄 คืนค่าเป็นวอลเปเปอร์เริ่มต้น", color = RubyRed, fontSize = 13.sp) },
                                onClick = {
                                    showWallpaperMenu = false
                                    onResetWallpaper()
                                }
                            )
                        }
                    }
                }

                // Update status chip & refresh icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                        .clickable(enabled = !isUpdating) { onRefreshUpdate() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    if (isUpdating) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Updating",
                            tint = NeonVioletLight,
                            modifier = Modifier
                                .size(14.dp)
                                .rotate(rotation)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = ytDlpVersion,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Subtitle status line
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = statusMessage,
            color = TextMuted.copy(alpha = 0.8f),
            fontSize = 11.sp
        )
    }
}
