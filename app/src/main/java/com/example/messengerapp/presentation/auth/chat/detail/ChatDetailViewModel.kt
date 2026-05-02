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
import com.example.domain.domain.usecase.PinMessageUseCase
import com.example.domain.domain.usecase.SearchMessagesUseCase
import com.example.domain.domain.usecase.SendImageMessageUseCase
import com.example.domain.domain.usecase.SendVideoMessageUseCase
import com.example.domain.domain.usecase.SetTypingStatusUseCase
import com.example.domain.domain.usecase.SendVoiceMessageUseCase
import com.example.domain.domain.usecase.ToggleReactionUseCase
import com.example.domain.domain.usecase.TranscribeVoiceUseCase
import com.example.domain.domain.usecase.TranslateTextUseCase
import com.example.messengerapp.presentation.auth.biometric.SessionAuthManager
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
import com.example.messengerapp.BuildConfig

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
    private val sendVideoMessageUseCase: SendVideoMessageUseCase,
    private val sendVoiceMessageUseCase: SendVoiceMessageUseCase,
    private val toggleReactionUseCase: ToggleReactionUseCase,
    private val sessionAuthManager: SessionAuthManager,
    private val searchMessagesUseCase: SearchMessagesUseCase,
    private val transcribeVoiceUseCase: TranscribeVoiceUseCase,
    private val pinMessageUseCase: PinMessageUseCase,
    private val translateTextUseCase: TranslateTextUseCase,
    savedStateHandle: SavedStateHandle,
): ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])
    private var messagesJob: Job? = null
    private val _state = MutableStateFlow(ChatDetailState(
        isBiometricAuthenticated = sessionAuthManager.isAppUnlocked
    ))
    val state = _state.asStateFlow()
    private var typingJob: Job? = null

    init {
        loadCurrentUser()
        observeMessages()
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
                _state.update { it.copy(isFavorite = chat.isFavorite) }
                val myId = _state.value.currentUserId
                if (myId.isBlank()) return@onEach

                val partnerId = chat.participants.firstOrNull { it != myId } ?: ""
                if (partnerId.isNotBlank()) {
                    val partnerResult = getUserByIdUseCase(partnerId)
                    partnerResult.onSuccess { user ->
                        _state.update {
                            it.copy(
                                opponentName = user?.username ?: "Неизвестный",
                                opponentImage = user?.photoUrl
                            )
                        }
                    }
                }

                val isOpponentTyping = chat.typing.entries.any { (userId, isTyping) ->
                    userId != myId && isTyping
                }
                _state.update { it.copy(isOpponentTyping = isOpponentTyping) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeMessages(query: String = "") {
        messagesJob?.cancel()

        messagesJob = if (query.isBlank()) {
            getMessagesUseCase(chatId)
        } else {
            searchMessagesUseCase(chatId, query)
        }
            .onStart { _state.update { it.copy(isLoading = true, error = null) } }
            .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            .onEach { messages ->
                _state.update { it.copy(isLoading = false, messages = messages) }
                markUnreadMessagesAsRead(messages)
                val pinned = messages.firstOrNull { it.isPinned }
                _state.update { it.copy(pinnedMessage = pinned) }
            }
            .launchIn(viewModelScope)
    }

    private fun pinMessage(messageId: String, pin: Boolean) {
        viewModelScope.launch {
            if (pin) {
                val currentPinned = _state.value.pinnedMessage
                if (currentPinned != null && currentPinned.id != messageId) {
                    pinMessageUseCase(chatId, currentPinned.id, false)
                }
            }
            pinMessageUseCase(chatId, messageId, pin)
                .onFailure { e -> _state.update { it.copy(error = "Ошибка закрепления: ${e.message}") } }
        }
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
        val replyId = currentState.replyToMessage?.id
        val replyText = currentState.replyToMessage?.text ?: currentState.replyToMessage?.imageUrl?.let { "Фотография" }

        _state.update { it.copy(messageText = "", replyToMessage = null) }

        viewModelScope.launch {
            try {
                sendMessageUseCase(
                    chatId = chatId,
                    text = textToSend,
                    userId = currentState.currentUserId,
                    replyToMessageId = replyId,
                    replyToMessageText = replyText
                )
            } catch (e: Exception) {
                _state.update { it.copy(error = e.toString(), messageText = textToSend) }
            }
        }
    }

    fun handleIntent(intent: ChatDetailIntent) {
        when(intent) {
            is ChatDetailIntent.OnTranscribeClick -> {
                viewModelScope.launch {
                    transcribeVoiceUseCase(chatId, intent.messageId, intent.voiceUrl)
                        .onFailure { e ->
                            _state.update { it.copy(error = e.message) }
                        }
                }
            }
            is ChatDetailIntent.OnPinMessage -> pinMessage(intent.messageId, intent.pin)
            is ChatDetailIntent.OnTranslateImageClick -> translateImageText(intent.messageId, intent.imageUrl)
            is ChatDetailIntent.OnGenerateSummaryClick -> generateChatSummary()
            is ChatDetailIntent.OnTranslateMessageClick -> translateMessage(intent.messageId, intent.text)
            ChatDetailIntent.OnCloseSummary -> _state.update { it.copy(chatSummary = null) }
            is ChatDetailIntent.OnSwipeToReply -> _state.update { it.copy(replyToMessage = intent.message) }
            ChatDetailIntent.OnCancelReply -> _state.update { it.copy(replyToMessage = null) }
            is ChatDetailIntent.OnVideoSelected -> sendVideo(intent.uri)
            is ChatDetailIntent.OnBiometricSuccess -> {
                sessionAuthManager.isAppUnlocked = true
                _state.update { it.copy(isBiometricAuthenticated = true) }
            }
            is ChatDetailIntent.OnMessageTextChanged -> {
                _state.update { it.copy(messageText = intent.text) }
                updateTypingStatus()
            }
            is ChatDetailIntent.OnSendClick -> sendMessage()
            ChatDetailIntent.OnToggleSearch -> {
                val willBeActive = !_state.value.isSearchActive
                _state.update { it.copy(isSearchActive = willBeActive, searchQuery = "") }
                observeMessages()
            }
            is ChatDetailIntent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = intent.query) }
                observeMessages(intent.query)
            }
            is ChatDetailIntent.OnImageSelected -> sendImage(intent.uri)
            is ChatDetailIntent.OnDeleteMessage -> deleteMessage(intent.messageId)
            is ChatDetailIntent.OnVoiceRecorded -> sendVoice(intent.uri, intent.duration, intent.text)
            is ChatDetailIntent.OnToggleReaction -> toggleReaction(intent.messageId, intent.emoji)
        }
    }

    private fun translateImageText(messageId: String, imageUrl: String) {
        _state.update { it.copy(loadingImageTranslations = it.loadingImageTranslations + messageId) }

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GROQ_API_KEY

                val resultText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val imageBytes = java.net.URL(imageUrl).readBytes()
                    val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)

                    val url = java.net.URL("https://api.groq.com/openai/v1/chat/completions")
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.setRequestProperty("Authorization", "Bearer $apiKey")
                    connection.doOutput = true

                    val jsonBody = """
                    {
                      "model": "meta-llama/llama-4-scout-17b-16e-instruct",
                      "messages": [
                        {
                          "role": "user",
                          "content": [
                            {
                              "type": "image_url",
                              "image_url": {
                                "url": "data:image/jpeg;base64,$base64Image"
                              }
                            },
                            {
                              "type": "text",
                              "text": "Извлеки весь текст с этого изображения и переведи его на русский язык. Если текста нет — опиши что изображено на картинке на русском языке."
                            }
                          ]
                        }
                      ],
                      "max_tokens": 1000
                    }
                """.trimIndent()

                    val writer = java.io.OutputStreamWriter(connection.outputStream)
                    writer.write(jsonBody)
                    writer.flush()
                    writer.close()

                    if (connection.responseCode == 200) {
                        val responseJson = connection.inputStream.bufferedReader().readText()
                        val jsonObject = org.json.JSONObject(responseJson)
                        jsonObject.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                    } else {
                        val errorTxt = connection.errorStream?.bufferedReader()?.readText() ?: "Нет деталей"
                        throw Exception("HTTP Ошибка: ${connection.responseCode}. $errorTxt")
                    }
                }

                _state.update { currentState ->
                    val newMap = currentState.translatedImageTexts.toMutableMap()
                    newMap[messageId] = resultText
                    currentState.copy(
                        translatedImageTexts = newMap,
                        loadingImageTranslations = currentState.loadingImageTranslations - messageId
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("GroqVision", "Ошибка: ", e)
                _state.update {
                    it.copy(
                        error = "Ошибка анализа фото: ${e.message}",
                        loadingImageTranslations = it.loadingImageTranslations - messageId
                    )
                }
            }
        }
    }

    private fun generateChatSummary() {
        val messages = _state.value.messages
        if (messages.isEmpty()) return

        _state.update { it.copy(isSummaryLoading = true) }

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GROQ_API_KEY

                val chatHistory = messages.filter { !it.text.isNullOrBlank() }
                    .take(30)
                    .joinToString("\n") {
                        val sender = if (it.senderId == _state.value.currentUserId) "Я" else "Собеседник"
                        "$sender: ${it.text}"
                    }

                val prompt = "Проанализируй этот диалог и напиши краткую выжимку в 2-3 предложениях (о чем договорились, главная суть):\\n$chatHistory"

                val resultText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val url = java.net.URL("https://api.groq.com/openai/v1/chat/completions")

                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.setRequestProperty("Authorization", "Bearer $apiKey")
                    connection.doOutput = true

                    val jsonBody = """
                    {
                      "model": "llama-3.3-70b-versatile",
                      "messages": [
                        {
                          "role": "user",
                          "content": "${prompt.replace("\n", "\\n").replace("\"", "\\\"")}"
                        }
                      ],
                      "max_tokens": 500
                    }
                """.trimIndent()

                    val writer = java.io.OutputStreamWriter(connection.outputStream)
                    writer.write(jsonBody)
                    writer.flush()
                    writer.close()

                    if (connection.responseCode == 200) {
                        val responseJson = connection.inputStream.bufferedReader().readText()
                        val jsonObject = org.json.JSONObject(responseJson)
                        jsonObject.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                    } else {
                        val errorTxt = connection.errorStream?.bufferedReader()?.readText() ?: "Нет деталей"
                        throw Exception("HTTP Ошибка: ${connection.responseCode}. $errorTxt")
                    }
                }

                _state.update { it.copy(chatSummary = resultText, isSummaryLoading = false) }
            } catch (e: Exception) {
                android.util.Log.e("GroqError", "Ошибка REST: ", e)
                _state.update { it.copy(error = "Ошибка ИИ: ${e.message}", isSummaryLoading = false) }
            }
        }
    }

    private fun translateMessage(messageId: String, text: String) {
        viewModelScope.launch {
            translateTextUseCase(text)
                .onSuccess { translatedText ->
                    _state.update { currentState ->
                        val newTranslations = currentState.translatedMessages.toMutableMap()
                        newTranslations[messageId] = translatedText
                        currentState.copy(translatedMessages = newTranslations)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Ошибка собственного ИИ-перевода: ${e.message}") }
                }
        }
    }

    private fun sendVoice(uri: Uri, duration: Int, text: String?) {
        viewModelScope.launch {
            sendVoiceMessageUseCase(chatId, uri, duration, text)
                .onFailure { e -> _state.update { it.copy(error = "Не удалось отправить аудио: ${e.message}") } }
        }
    }

    private fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            toggleReactionUseCase(chatId, messageId, emoji)
                .onFailure { e -> _state.update { it.copy(error = "Ошибка реакции: ${e.message}") } }
        }
    }

    private fun sendImage(uri: Uri) {
        viewModelScope.launch {
            sendImageMessageUseCase(chatId, uri)
                .onFailure { e -> _state.update { it.copy(error = "Не удалось отправить фото: ${e.message}") } }
        }
    }

    private fun sendVideo(uri: Uri){
        viewModelScope.launch {
            sendVideoMessageUseCase(chatId,uri)
                .onFailure{ e-> _state.update { it.copy(error = "Не удалось отправить видео! :${e.message}" ) } }
        }
    }

    fun openFullscreenImage(url: String) { _state.update { it.copy(fullscreenImageUrl = url) } }
    fun closeFullscreenImage() { _state.update { it.copy(fullscreenImageUrl = null) } }

    private fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            deleteMessageUseCase(chatId, messageId).onFailure { e -> _state.update { it.copy(error = "Ошибка удаления: ${e.message}") } }
        }
    }

    private fun updateTypingStatus() {
        if (typingJob == null) {
            viewModelScope.launch(Dispatchers.IO) { setTypingStatusUseCase(chatId, true) }
        }
        typingJob?.cancel()
        typingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(3000)
                setTypingStatusUseCase(chatId, false)
                typingJob = null
            } catch (e: CancellationException) { throw e }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO + NonCancellable) { setTypingStatusUseCase(chatId, false) }
    }
}