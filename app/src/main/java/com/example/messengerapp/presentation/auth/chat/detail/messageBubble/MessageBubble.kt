package com.example.messengerapp.presentation.auth.chat.detail.messageBubble

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.domain.model.Message
import com.example.messengerapp.app.utils.DateUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    val alignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart

    val backgroundColor = if (isOwnMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOwnMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant


    val shape = if (isOwnMessage) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = alignment
    ) {

        Surface(
            shape = shape,
            color = backgroundColor,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(
                    onClick = { },
                    onLongClick = { if (isOwnMessage) showMenu = true }
                )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (message.imageUrl != null) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    if (!message.text.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }


                if (!message.text.isNullOrBlank()) {
                    Text(
                        text = message.text!!,
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.formatTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.6f)
                    )

                    if (isOwnMessage) {
                        Spacer(modifier = Modifier.width(4.dp))
                        val icon = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check
                        val iconColor = if (message.isRead) Color(0xFF2196F3) else textColor.copy(alpha = 0.6f)

                        Icon(
                            imageVector = icon,
                            contentDescription = "Status",
                            tint = iconColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        DropdownMenu(  //
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Удалить сообщение") },
                onClick = {
                    onDeleteClick()
                    showMenu = false
                }
            )
        }
    }
}