package com.example.messengerapp.presentation.auth.chat.detail.messageBubble

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.domain.model.Message

@Composable
fun MessageBubble (
    message: Message,
    isOwnMessage: Boolean
    ){
    val alignment = if(isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    val color = if(isOwnMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOwnMessage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 8.dp,vertical =  4.dp),
        contentAlignment = alignment
    ){
        Surface(
            shadowElevation = 2.dp,
            color = color,
            shape = shape
        ) {
            Text(
                text = message.text,
                color = textColor,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}