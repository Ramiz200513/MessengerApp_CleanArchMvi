package com.example.messengerapp.presentation.auth.chat.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.domain.usecase.GetCurrentUserUseCase
import com.example.domain.domain.usecase.SendMessageUseCase
import com.example.domain.domain.usecase.GetMessagesUseCase
import com.example.domain.domain.usecase.ObserveChatUseCase
import com.example.domain.domain.usecase.SetTypingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val setTypingStatusUseCase: SetTypingStatusUseCase,
    private val observeChatUseCase: ObserveChatUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle,
): ViewModel() {

    var messageText by mutableStateOf("")
        private set // FIX: Закрываем сеттер, чтобы менять только через Intent

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])

    private val _state = MutableStateFlow(ChatDetailState())
    val state = _state.asStateFlow()

    private var typingJob: Job? = null

    // Локальная переменная для ID, чтобы не дергать UseCase постоянно
    private var myUserId: String = ""

    init {
        loadCurrentUser()
        loadMessage(chatId)
        observeChatStatus()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val result = getCurrentUserUseCase()
            myUserId = result.getOrNull()?.id ?: ""
            _state.update { it.copy(currentUserId = myUserId) }
        }
    }

    private fun observeChatStatus() {
        observeChatUseCase(chatId)
            .onEach { chat ->
                if (myUserId.isBlank()) {
                    val result = getCurrentUserUseCase()
                    myUserId = result.getOrNull()?.id ?: ""
                }

                if (myUserId.isBlank()) return@onEach

                val isOpponentTyping = chat.typing.entries.any { (userId, isTyping) ->
                    userId != myUserId && isTyping
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
                    // FIX: Исправил message -> messages (проверь название в State!)
                    it.copy(isLoading = false, messages = messages)
                }
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage() {
        if (myUserId.isBlank()) return
        if (messageText.isBlank()) return

        val textToSend = messageText // Сохраняем текст для отправки
        messageText = "" // FIX: Очищаем поле СРАЗУ, чтобы юзер не спамил

        viewModelScope.launch {
            try {
                sendMessageUseCase(chatId = chatId, text = textToSend, userId = myUserId)
                // Успех
            } catch (e: Exception) {
                _state.update { it.copy(error = "Не удалось отправить") }
                messageText = textToSend // FIX: Если ошибка - возвращаем текст обратно
            }
        }
    }

    fun handleIntent(intent: ChatDetailIntent) {
        when(intent) {
            is ChatDetailIntent.OnMessageTextChanged -> {
                messageText = intent.text
                updateTypingStatus()
            }
            is ChatDetailIntent.OnSendClick -> {
                sendMessage()
            }
            ChatDetailIntent.LoadMessages -> {
                loadMessage(chatId) // FIX: Убрали TODO
            }
        }
    }

    private fun updateTypingStatus() {

        if (typingJob == null) {
            viewModelScope.launch {
                setTypingStatusUseCase(chatId, true)
            }
        }
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(3000)
            setTypingStatusUseCase(chatId, false)
            typingJob = null
        }
    }
}