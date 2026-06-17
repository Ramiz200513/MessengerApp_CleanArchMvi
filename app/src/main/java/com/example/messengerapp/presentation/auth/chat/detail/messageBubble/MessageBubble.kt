package com.example.messengerapp.presentation.auth.chat.detail.messageBubble

import androidx.media3.common.MediaItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    audioPlayer: AndroidAudioPlayer? = null,
    onTranscribeClick: (String, String) -> Unit,
    translatedText: String? = null,
    onTranslateClick: (String, String) -> Unit,
    translatedImageText: String? = null,
    isImageTranslating: Boolean = false,
    onTranslateImageClick: (String, String) -> Unit = { _, _ -> },
    onPinClick: (String, Boolean) -> Unit = { _, _ -> }
) {
    var showReactionPicker by remember { mutableStateOf(false) }

    val ownBubbleColor = MaterialTheme.colorScheme.primaryContainer
    val otherBubbleColor = MaterialTheme.colorScheme.surfaceVariant
    val backgroundColor = if (isOwnMessage) ownBubbleColor else otherBubbleColor
    val textColor = if (isOwnMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    val shape = if (isOwnMessage) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    }

    val emojis = listOf("👍", "❤️", "😂", "😮", "😢", "🔥")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isOwnMessage) 48.dp else 8.dp,
                end = if (isOwnMessage) 8.dp else 48.dp,
                top = 2.dp,
                bottom = 2.dp
            ),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        // Индикатор закреплённого
        if (message.isPinned) {
            Text(
                text = "📌",
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        Surface(
            shape = shape,
            color = backgroundColor,
            shadowElevation = 2.dp,
            modifier = Modifier
                .combinedClickable(
                    onClick = { },
                    onLongClick = { showReactionPicker = true }
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {

                // Блок ответа
                if (message.replyToMessageId != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                            )
                            .padding(start = 10.dp, top = 6.dp, end = 8.dp, bottom = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = "Ответ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = message.replyToMessageText ?: "Вложение",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Фото
                if (message.imageUrl != null) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 220.dp)
                                .clickable { onImageClick(message.imageUrl!!) }
                        )
                        // Затемнение сверху для кнопки
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)
                                    )
                                )
                        )
                        IconButton(
                            onClick = { onTranslateImageClick(message.id, message.imageUrl!!) },
                            enabled = !isImageTranslating,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(30.dp)
                        ) {
                            if (isImageTranslating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("🔍", fontSize = 16.sp)
                            }
                        }
                    }

                    if (translatedImageText != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🖼️ $translatedImageText",
                            color = textColor.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                    if (!message.text.isNullOrBlank()) Spacer(modifier = Modifier.height(6.dp))
                }

                // Видео
                if (message.videoUrl != null) {
                    VideoPlayer(
                        url = message.videoUrl!!,
                        modifier = Modifier
                            .padding(bottom = if (!message.text.isNullOrBlank()) 6.dp else 0.dp)
                    )
                }

                // Голосовое
                if (message.voiceUrl != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        VoiceMessagePlayer(
                            url = message.voiceUrl!!,
                            duration = message.voiceDuration ?: 0,
                            audioPlayer = audioPlayer,
                            textColor = textColor
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { onTranscribeClick(message.id, message.voiceUrl!!) },
                            enabled = !message.isTranscribing,
                            modifier = Modifier.size(36.dp)
                        ) {
                            if (message.isTranscribing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Аа",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (message.voiceTranscription != null)
                                        textColor.copy(alpha = 0.35f)
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (message.voiceTranscription != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.voiceTranscription!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(textColor.copy(alpha = 0.05f))
                                .padding(8.dp)
                        )
                    }
                }

                // Текст
                if (!message.text.isNullOrBlank()) {
                    Text(
                        text = message.text!!,
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                    if (translatedText != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(
                            color = textColor.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🇷🇺 $translatedText",
                            color = textColor.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp
                        )
                    }
                }

                // Время + статус
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = DateUtils.formatTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.5f)
                    )
                    if (isOwnMessage) {
                        val icon = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check
                        val iconColor = if (message.isRead) Color(0xFF2196F3) else textColor.copy(alpha = 0.5f)
                        Icon(
                            imageVector = icon,
                            contentDescription = "Status",
                            tint = iconColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // Реакции
        if (message.reactions.isNotEmpty()) {
            ReactionsRow(
                reactions = message.reactions,
                onReactionClick = onToggleReaction,
                isOwnMessage = isOwnMessage
            )
        }

        if (showReactionPicker) {
            ReactionPicker(
                emojis = emojis,
                onEmojiSelected = { onToggleReaction(it); showReactionPicker = false },
                onDismiss = { showReactionPicker = false },
                onDeleteClick = { onDeleteClick(); showReactionPicker = false },
                onTranslateClick = {
                    if (!message.text.isNullOrBlank()) onTranslateClick(message.id, message.text!!)
                },
                isPinned = message.isPinned,
                onPinClick = { onPinClick(message.id, !message.isPinned) }
            )
        }
    }
}

@Composable
fun VoiceMessagePlayer(url: String, duration: Int, audioPlayer: AndroidAudioPlayer?, textColor: Color) {
    var isPlaying by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(50))
                .background(textColor.copy(alpha = 0.1f))
                .clickable {
                    if (isPlaying) { audioPlayer?.stop(); isPlaying = false }
                    else { audioPlayer?.playFile(url); isPlaying = true }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            LinearProgressIndicator(
                progress = { 0f },
                modifier = Modifier
                    .width(130.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = textColor,
                trackColor = textColor.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${duration / 60}:${String.format("%02d", duration % 60)}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
fun ReactionsRow(
    reactions: Map<String, List<String>>,
    onReactionClick: (String) -> Unit,
    isOwnMessage: Boolean = false
) {
    Row(
        modifier = Modifier.padding(top = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        reactions.forEach { (emoji, users) ->
            if (users.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.clickable { onReactionClick(emoji) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = emoji, fontSize = 13.sp)
                        if (users.size > 1) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = users.size.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
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
    onDeleteClick: () -> Unit,
    onTranslateClick: () -> Unit,
    isPinned: Boolean = false,
    onPinClick: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            // Эмодзи пикер вверху
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                emojis.forEach { emoji ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { onEmojiSelected(emoji) }
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        },
        text = {
            // Кнопки действий
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(4.dp))

                // Закрепить/открепить
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPinClick(); onDismiss() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📌", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isPinned) "Открепить" else "Закрепить",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Перевести
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTranslateClick(); onDismiss() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌐", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Перевести",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Удалить
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDeleteClick(); onDismiss() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🗑️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Удалить",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {}
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
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { PlayerView(it).apply { player = exoPlayer; useController = true } },
            modifier = Modifier.fillMaxSize()
        )
    }
}