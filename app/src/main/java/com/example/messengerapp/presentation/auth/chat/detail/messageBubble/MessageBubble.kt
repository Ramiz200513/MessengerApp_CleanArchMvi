package com.example.messengerapp.presentation.auth.chat.detail.messageBubble

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.domain.model.Message
import com.example.messengerapp.app.utils.DateUtils

@Composable
fun MessageBubble (
    message: Message,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier
    ){
    val alignment = if(isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    val color = if(isOwnMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOwnMessage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ){
        Surface(
            shadowElevation = 2.dp,
            color = color,
            shape = shape
        ) {
            Row(modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Bottom) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = DateUtils.formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f) // Цвет подстраиваем под фон
                )
            }
        }
    }
}