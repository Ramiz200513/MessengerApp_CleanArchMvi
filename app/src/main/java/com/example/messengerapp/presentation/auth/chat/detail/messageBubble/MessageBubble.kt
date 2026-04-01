package com.example.messengerapp.presentation.auth.chat.detail.messageBubble


import androidx.media3.common.MediaItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.domain.domain.model.Message
import com.example.messengerapp.app.utils.DateUtils
import com.example.messengerapp.data.utils.AndroidAudioPlayer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onToggleReaction: (String) -> Unit = {},
    audioPlayer: AndroidAudioPlayer? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }

    val alignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isOwnMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOwnMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    val shape = if (isOwnMessage) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val emojis = listOf("👍", "❤️", "😂", "😮", "😢", "🔥")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = shape,
            color = backgroundColor,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(
                    onClick = { },
                    onLongClick = { showReactionPicker = true }
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
                            .clickable { onImageClick(message.imageUrl!!) }
                    )
                    if (!message.text.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                if (message.videoUrl != null) {
                    VideoPlayer(
                        url = message.videoUrl!!,
                        modifier = Modifier.padding(bottom = if (!message.text.isNullOrBlank()) 6.dp else 0.dp)
                    )
                }

                if (message.voiceUrl != null) {
                    VoiceMessagePlayer(
                        url = message.voiceUrl!!,
                        duration = message.voiceDuration ?: 0,
                        audioPlayer = audioPlayer,
                        textColor = textColor
                    )
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

        if (message.reactions.isNotEmpty()) {
            ReactionsRow(reactions = message.reactions, onReactionClick = onToggleReaction)
        }

        if (showReactionPicker) {
            ReactionPicker(
                emojis = emojis,
                onEmojiSelected = {
                    onToggleReaction(it)
                    showReactionPicker = false
                },
                onDismiss = { showReactionPicker = false },
                onDeleteClick = {
                    onDeleteClick()
                    showReactionPicker = false
                }
            )
        }
    }
}

@Composable
fun VoiceMessagePlayer(
    url: String,
    duration: Int,
    audioPlayer: AndroidAudioPlayer?,
    textColor: Color
) {
    var isPlaying by remember { mutableStateOf(false) }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        IconButton(onClick = {
            if (isPlaying) {
                audioPlayer?.stop()
                isPlaying = false
            } else {
                audioPlayer?.playFile(url)
                isPlaying = true
            }
        }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                tint = textColor
            )
        }
        
        Column {
            LinearProgressIndicator(
                progress = { 0f },
                modifier = Modifier.width(150.dp),
                color = textColor,
                trackColor = textColor.copy(alpha = 0.2f)
            )
            Text(
                text = "${duration / 60}:${String.format("%02d", duration % 60)}",
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ReactionsRow(
    reactions: Map<String, List<String>>,
    onReactionClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        reactions.forEach { (emoji, users) ->
            if (users.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onReactionClick(emoji) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = emoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = users.size.toString(), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionPicker(
    emojis: List<String>,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDeleteClick) {
                Text("Удалить сообщение", color = Color.Red)
            }
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                emojis.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .clickable { onEmojiSelected(emoji) }
                            .padding(4.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun VideoPlayer(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
    ) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}