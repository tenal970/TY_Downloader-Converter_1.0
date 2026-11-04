package com.tenal.yaoitron.ui.components

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenal.yaoitron.ui.theme.CardBorder
import com.tenal.yaoitron.ui.theme.CardSurface
import com.tenal.yaoitron.ui.theme.EmeraldGreen
import com.tenal.yaoitron.ui.theme.NeonViolet
import com.tenal.yaoitron.ui.theme.TextMuted
import com.tenal.yaoitron.ui.theme.TextWhite

@Composable
fun CustomFolderDialog(
    initialPath: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var path by remember {
        mutableStateOf(
            initialPath.ifBlank {
                java.io.File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "YAOITRON"
                ).absolutePath
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📁 กำหนดโฟลเดอร์ปลายทางเอง",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "ระบุพาธโฟลเดอร์ในเครื่องที่ต้องการให้บันทึกไฟล์ลงไป:",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
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
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(path.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ตกลง", color = TextWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก", color = TextMuted)
            }
        },
        containerColor = CardSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(14.dp))
    )
}
