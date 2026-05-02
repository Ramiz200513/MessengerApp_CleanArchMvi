package com.example.messengerapp.presentation.auth.chat.detail

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.messengerapp.R
import com.example.messengerapp.data.utils.AndroidAudioPlayer
import com.example.messengerapp.data.utils.AndroidAudioRecorder
import com.example.messengerapp.presentation.auth.biometric.BiometricManager
import com.example.messengerapp.presentation.auth.chat.detail.messageBubble.FullscreenImageViewer
import com.example.messengerapp.presentation.auth.chat.detail.messageBubble.MessageBubble
import com.example.messengerapp.presentation.auth.chat.detail.messageBubble.MessageInput
import com.example.messengerapp.presentation.auth.chat.detail.messageBubble.SwipeToReplyWrapper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val activity = remember(context) {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is FragmentActivity) break
            currentContext = currentContext.baseContext
        }
        currentContext as? FragmentActivity
    }

    val recorder = remember { AndroidAudioRecorder(context) }
    val audioPlayer = remember { AndroidAudioPlayer(context) }
    var voiceFile by remember { mutableStateOf<File?>(null) }
    var recordStartTime by remember { mutableLongStateOf(0L) }
    var recognizedVoiceText by remember { mutableStateOf<String?>(null) }

    val biometricManager = remember(activity) {
        activity?.let { BiometricManager(it) }
    }

    var isAuthInProgress by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isFavorite, state.isBiometricAuthenticated) {
        if (state.isFavorite && !state.isBiometricAuthenticated && !isAuthInProgress) {
            isAuthInProgress = true
            biometricManager?.showBiometricPrompt(
                onSuccess = {
                    isAuthInProgress = false
                    viewModel.handleIntent(ChatDetailIntent.OnBiometricSuccess)
                },
                onError = { error ->
                    isAuthInProgress = false
                    if (error.contains("отмена", true) || error.contains("cancel", true) || error.contains("back", true)) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) viewModel.handleIntent(ChatDetailIntent.OnImageSelected(uri)) }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) viewModel.handleIntent(ChatDetailIntent.OnVideoSelected(uri)) }
    )

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (state.isSearchActive) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.handleIntent(ChatDetailIntent.OnSearchQueryChanged(it)) },
                            placeholder = { Text("Поиск...", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.handleIntent(ChatDetailIntent.OnToggleSearch) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
                        }
                    }
                )
            } else {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = state.opponentImage,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                error = painterResource(R.drawable.ic_launcher_foreground),
                                placeholder = painterResource(R.drawable.ic_launcher_foreground)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = state.opponentName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                AnimatedVisibility(visible = state.isOpponentTyping) {
                                    Text(
                                        text = "печатает...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.handleIntent(ChatDetailIntent.OnGenerateSummaryClick) },
                            enabled = !state.isSummaryLoading
                        ) {
                            if (state.isSummaryLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Summary")
                            }
                        }
                        IconButton(onClick = { viewModel.handleIntent(ChatDetailIntent.OnToggleSearch) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                AnimatedVisibility(visible = state.pinnedMessage != null) {
                    state.pinnedMessage?.let { pinned ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "📌 Закреплённое",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = pinned.text
                                            ?: if (pinned.imageUrl != null) "Фотография"
                                            else if (pinned.voiceUrl != null) "Голосовое сообщение"
                                            else "Медиа",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.handleIntent(ChatDetailIntent.OnPinMessage(pinned.id, false)) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Открепить",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        SwipeToReplyWrapper(
                            onSwipe = { viewModel.handleIntent(ChatDetailIntent.OnSwipeToReply(message)) }
                        ) {
                            MessageBubble(
                                message = message,
                                isOwnMessage = message.senderId == state.currentUserId,
                                onDeleteClick = { viewModel.handleIntent(ChatDetailIntent.OnDeleteMessage(message.id)) },
                                onImageClick = { viewModel.openFullscreenImage(it) },
                                onToggleReaction = { emoji -> viewModel.handleIntent(ChatDetailIntent.OnToggleReaction(message.id, emoji)) },
                                onTranscribeClick = { msgId, url -> viewModel.handleIntent(ChatDetailIntent.OnTranscribeClick(msgId, url)) },
                                audioPlayer = audioPlayer,
                                translatedText = state.translatedMessages[message.id],
                                onTranslateClick = { msgId, text -> viewModel.handleIntent(ChatDetailIntent.OnTranslateMessageClick(msgId, text)) },
                                translatedImageText = state.translatedImageTexts[message.id],
                                isImageTranslating = state.loadingImageTranslations.contains(message.id),
                                onTranslateImageClick = { msgId, url -> viewModel.handleIntent(ChatDetailIntent.OnTranslateImageClick(msgId, url)) },
                                onPinClick = { msgId, pin -> viewModel.handleIntent(ChatDetailIntent.OnPinMessage(msgId, pin)) }
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = state.replyToMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ответ",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = state.replyToMessage?.text ?: "Медиа сообщение",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.handleIntent(ChatDetailIntent.OnCancelReply) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 3. ПОЛЕ ВВОДА
                MessageInput(
                    text = state.messageText,
                    onTextChange = { viewModel.handleIntent(ChatDetailIntent.OnMessageTextChanged(it)) },
                    onSendClick = { viewModel.handleIntent(ChatDetailIntent.OnSendClick) },
                    onAttachClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onVideoClick = { videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                    onVoiceStart = {
                        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            val file = File(context.cacheDir, "voice_record.m4a")
                            voiceFile = file
                            recordStartTime = System.currentTimeMillis()
                            recognizedVoiceText = null
                            try {
                                recorder.start(file) { text ->
                                    recognizedVoiceText = text.takeIf { it.isNotBlank() }
                                }
                            } catch (e: Exception) { }
                        }
                    },
                    onVoiceStop = {
                        try {
                            recorder.stop()
                            val duration = ((System.currentTimeMillis() - recordStartTime) / 1000).toInt()
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (duration > 0) {
                                    voiceFile?.let { file ->
                                        viewModel.handleIntent(
                                            ChatDetailIntent.OnVoiceRecorded(
                                                uri = Uri.fromFile(file),
                                                duration = duration,
                                                text = recognizedVoiceText
                                            )
                                        )
                                    }
                                }
                            }, 500)
                        } catch (e: Exception) { }
                    }
                )
            }

            if (state.chatSummary != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.handleIntent(ChatDetailIntent.OnCloseSummary) },
                    title = { Text("Выжимка чата 🤖") },
                    text = { Text(state.chatSummary ?: "") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.handleIntent(ChatDetailIntent.OnCloseSummary) }) {
                            Text("Понятно", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }

            state.fullscreenImageUrl?.let { url ->
                FullscreenImageViewer(imageUrl = url, onDismiss = { viewModel.closeFullscreenImage() })
            }
        }
    }
}