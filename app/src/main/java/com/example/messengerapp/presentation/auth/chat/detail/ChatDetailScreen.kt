package com.example.messengerapp.presentation.auth.chat.detail
import com.example.messengerapp.R
import android.net.Uri
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.messengerapp.data.utils.AndroidAudioPlayer
import com.example.messengerapp.data.utils.AndroidAudioRecorder
import com.example.messengerapp.presentation.auth.chat.detail.messageBubble.FullscreenImageViewer
import com.example.messengerapp.presentation.auth.chat.detail.messageBubble.MessageBubble
import com.example.messengerapp.presentation.auth.chat.detail.messageBubble.MessageInput
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
    
    val recorder = remember { AndroidAudioRecorder(context) }
    val audioPlayer = remember { AndroidAudioPlayer(context) }
    var voiceFile by remember { mutableStateOf<File?>(null) }
    var recordStartTime by remember { mutableLongStateOf(0L) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Можно показать Snackbar, что запись невозможна без микрофона
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

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = state.opponentImage,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                error = painterResource(R.drawable.ic_launcher_foreground),
                                placeholder = painterResource(R.drawable.ic_launcher_foreground)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = state.opponentName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                AnimatedVisibility(
                                    visible = state.isOpponentTyping,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
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
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
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
                        MessageBubble(
                            message = message,
                            isOwnMessage = message.senderId == state.currentUserId,
                            modifier = Modifier.animateItem(),
                            onDeleteClick = { viewModel.handleIntent(ChatDetailIntent.OnDeleteMessage(message.id)) },
                            onImageClick = { viewModel.openFullscreenImage(it) },
                            onToggleReaction = { emoji -> 
                                viewModel.handleIntent(ChatDetailIntent.OnToggleReaction(message.id, emoji)) 
                            },
                            audioPlayer = audioPlayer
                        )
                    }
                }

                MessageInput(
                    text = state.messageText,
                    onTextChange = { viewModel.handleIntent(ChatDetailIntent.OnMessageTextChanged(it)) },
                    onSendClick = { viewModel.handleIntent(ChatDetailIntent.OnSendClick) },
                    onAttachClick = {
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onVideoClick = {
                        videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    },
                    onVoiceStart = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            val file = File(context.cacheDir, "voice_record.m4a")
                            voiceFile = file
                            recordStartTime = System.currentTimeMillis()
                            try {
                                recorder.start(file)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onVoiceStop = {
                        try {
                            recorder.stop()
                            val duration = ((System.currentTimeMillis() - recordStartTime) / 1000).toInt()
                            if (duration > 0) {
                                voiceFile?.let {
                                    viewModel.handleIntent(ChatDetailIntent.OnVoiceRecorded(Uri.fromFile(it), duration))
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                )

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }
            }
        }

        state.fullscreenImageUrl?.let { url ->
            FullscreenImageViewer(
                imageUrl = url,
                onDismiss = { viewModel.closeFullscreenImage() }
            )
        }

    }
}