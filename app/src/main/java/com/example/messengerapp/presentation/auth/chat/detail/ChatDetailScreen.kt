package com.example.messengerapp.presentation.auth.chat.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.messengerapp.presentation.auth.chat.detail.messageBubble.MessageBubble
import com.example.messengerapp.presentation.auth.chat.detail.messageBubble.MessageInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    viewModel: ChatDetailViewModel = hiltViewModel(),
    chatId:String
) {
    val state by viewModel.state.collectAsState()
    val myUserId = state.currentUserId
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чат...") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                text = viewModel.messageText,
                onTextChange = {
                    viewModel.handleIntent(ChatDetailIntent.OnMessageTextChanged(it))
                },
                onSendClick = {
                    viewModel.handleIntent(ChatDetailIntent.OnSendClick)
                },
                modifier = Modifier.imePadding() // 🔥 клавиатура
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true,
                contentPadding = PaddingValues(8.dp)
            ) {
                items(state.message) { message ->
                    MessageBubble(message, isOwnMessage = message.senderId == myUserId)
                }

            }

        }


        if (state.error != null) {

            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }


    }

}