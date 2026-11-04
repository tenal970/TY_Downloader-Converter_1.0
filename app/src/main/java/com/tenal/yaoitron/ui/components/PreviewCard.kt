package com.tenal.yaoitron.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tenal.yaoitron.data.PreviewInfo
import com.tenal.yaoitron.ui.theme.CardBorder
import com.tenal.yaoitron.ui.theme.CardSurface
import com.tenal.yaoitron.ui.theme.EmeraldGreen
import com.tenal.yaoitron.ui.theme.NeonVioletLight
import com.tenal.yaoitron.ui.theme.RubyRed
import com.tenal.yaoitron.ui.theme.TextMuted
import com.tenal.yaoitron.ui.theme.TextWhite

@Composable
fun PreviewCard(
    info: PreviewInfo,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail
            if (info.thumbUrl.isNotBlank()) {
                AsyncImage(
                    model = info.thumbUrl,
                    contentDescription = info.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Title
            Text(
                text = info.title.ifEmpty { "(ไม่มีชื่อ)" },
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )

            // Metadata: Site, Uploader, Duration
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (info.site.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonVioletLight.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = info.site,
                            color = NeonVioletLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                val metaText = listOfNotNull(
                    info.uploader.ifBlank { null },
                    info.formattedDuration().ifBlank { null }
                ).joinToString(" · ")

                Text(
                    text = metaText,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            // Confirmation Question
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "ใช่วิดีโอที่ต้องการไหม?",
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "✅ ใช่ เริ่มโหลดเลย", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = RubyRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "❌ ไม่ใช่ ยกเลิก", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
