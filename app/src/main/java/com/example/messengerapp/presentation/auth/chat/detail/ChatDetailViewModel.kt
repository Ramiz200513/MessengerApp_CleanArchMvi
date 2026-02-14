package com.example.messengerapp.presentation.auth.chat.detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.domain.model.Message
import com.example.domain.domain.usecase.DeleteMessageUseCase
import com.example.domain.domain.usecase.GetCurrentUserUseCase
import com.example.domain.domain.usecase.SendMessageUseCase
import com.example.domain.domain.usecase.GetMessagesUseCase
import com.example.domain.domain.usecase.GetUserByIdUseCase
import com.example.domain.domain.usecase.MarkMessageAsReadUseCase
import com.example.domain.domain.usecase.ObserveChatUseCase
import com.example.domain.domain.usecase.SendImageMessageUseCase
import com.example.domain.domain.usecase.SetTypingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val setTypingStatusUseCase: SetTypingStatusUseCase,
    private val observeChatUseCase: ObserveChatUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sendImageMessageUseCase: SendImageMessageUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val markMessageAsReadUseCase: MarkMessageAsReadUseCase,
    savedStateHandle: SavedStateHandle,
    ): ViewModel() {


    private val chatId: String = checkNotNull(savedStateHandle["chatId"])

    private val _state = MutableStateFlow(ChatDetailState())
    val state = _state.asStateFlow()
    private var typingJob: Job? = null

    init {
        loadCurrentUser()
        loadMessage(chatId)
        observeChatStatus()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val result = getCurrentUserUseCase()
            val userId = result.getOrNull()?.id ?: ""
            _state.update { it.copy(currentUserId = userId) }
            if (userId.isNotBlank()) {
                markUnreadMessagesAsRead(_state.value.messages)
            }
        }
    }

    private fun observeChatStatus() {
        observeChatUseCase(chatId)
            .onEach { chat ->
                val myId = _state.value.currentUserId
                if (myId.isBlank()) return@onEach
                val partnerId = chat.participants.firstOrNull { it != myId } ?: ""

                if (partnerId.isNotBlank()) {
                    val partnerResult = getUserByIdUseCase(partnerId)
                    partnerResult.onSuccess { user ->
                        _state.update { it.copy(opponentName = user?.username ?: "Неизвестный") }
                    }
                }


                val isOpponentTyping = chat.typing.entries.any { (userId, isTyping) ->
                    userId != myId && isTyping
                }
                _state.update { it.copy(isOpponentTyping = isOpponentTyping) }
            }
            .launchIn(viewModelScope)
    }

    fun loadMessage(chatId: String) {
        getMessagesUseCase(chatId = chatId)
            .onStart {
                _state.update { it.copy(isLoading = true, error = null) }
            }
            .catch { exception ->
                _state.update {
                    it.copy(isLoading = false, error = exception.message ?: "Ошибка")
                }
            }
            .onEach { messages ->
                _state.update {
                    it.copy(isLoading = false, messages = messages)
                }
                markUnreadMessagesAsRead(messages)
            }
            .launchIn(viewModelScope)
    }
    private fun markUnreadMessagesAsRead(messages: List<Message>) {
        val myId = _state.value.currentUserId
        if (myId.isBlank()) return

        val unreadMessages = messages.filter { message ->
            message.senderId != myId && !message.isRead
        }

        if (unreadMessages.isNotEmpty()) {
            viewModelScope.launch {
                unreadMessages.forEach { message ->
                    markMessageAsReadUseCase(chatId, message.id)
                }
            }
        }
    }
    private fun sendMessage() {
        val currentState = _state.value
        if (currentState.currentUserId.isBlank()) return
        if (currentState.messageText.isBlank()) return
        val textToSend = currentState.messageText

        _state.update { it.copy(messageText = "") }

        viewModelScope.launch {
            try {
                sendMessageUseCase(chatId = chatId, text = textToSend, userId = currentState.currentUserId)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.toString(), messageText = textToSend) }
            }
        }
    }

    fun handleIntent(intent: ChatDetailIntent) {
        when(intent) {
            is ChatDetailIntent.OnMessageTextChanged -> {
                _state.update { it.copy(messageText = intent.text) }
                updateTypingStatus()
            }
            is ChatDetailIntent.OnSendClick -> {
                sendMessage()
            }
            ChatDetailIntent.LoadMessages -> {
                loadMessage(chatId)
            }
            is ChatDetailIntent.OnImageSelected -> {
                sendImage(intent.uri)
            }
            is ChatDetailIntent.OnDeleteMessage -> {
                deleteMessage(intent.messageId)
            }
        }
    }
    private fun sendImage(uri: Uri) {
        viewModelScope.launch {
            sendImageMessageUseCase(chatId, uri)
                .onFailure { e ->
                    _state.update { it.copy(error = "Не удалось отправить фото: ${e.message}") }
                }
        }
    }
    private fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            deleteMessageUseCase(chatId, messageId).onFailure { e ->
                _state.update { it.copy(error = "Ошибка удаления: ${e.message}") }
            }
        }
    }
    private fun updateTypingStatus() {
        if (typingJob == null) {
            viewModelScope.launch(Dispatchers.IO) {
                setTypingStatusUseCase(chatId, true)
            }
        }
        typingJob?.cancel()

        typingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(3000)
                setTypingStatusUseCase(chatId, false)
                typingJob = null
            } catch (e: CancellationException) {
                throw e
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            setTypingStatusUseCase(chatId, false)
        }
    }
}