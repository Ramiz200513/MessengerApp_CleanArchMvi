package com.example.messengerapp.presentation.auth.chat.detail.messageBubble

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    onVideoClick: () -> Unit,
    onVoiceStart: () -> Unit,
    onVoiceStop: () -> Unit,
) {
    var isRecording by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAttachClick) {
            Icon(Icons.Default.AttachFile, contentDescription = "Attach")
        }

        IconButton(onClick = onVideoClick) {
            Icon(Icons.Default.VideoCall, contentDescription = "Видео")
        }

        OutlinedTextField(
            value = if (isRecording) "Запись..." else text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            placeholder = { Text("Сообщение") },
            maxLines = 4,
            readOnly = isRecording,
            shape = RoundedCornerShape(24.dp)
        )

        if (text.isBlank()) {
            // Используем Box вместо IconButton для точного отслеживания нажатия
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color.Red.copy(alpha = 0.1f) else Color.Transparent)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitFirstDown() // Срабатывает сразу при касании
                                isRecording = true
                                onVoiceStart()
                                
                                waitForUpOrCancellation() // Ждем, пока отпустит
                                isRecording = false
                                onVoiceStop()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                )
            }
        } else {
            IconButton(onClick = onSendClick) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}