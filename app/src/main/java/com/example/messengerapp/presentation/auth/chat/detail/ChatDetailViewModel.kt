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
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle,
): ViewModel() {

    var messageText by mutableStateOf("")
    private val chatId:String = checkNotNull(savedStateHandle["chatId"])
    private val _state  = MutableStateFlow(ChatDetailState())
    val state = _state.asStateFlow()
    init {
        loadCurrentUser()
        loadMessage(chatId)
    }
    private fun loadCurrentUser() {
        viewModelScope.launch {
            // UseCase возвращает Result<User?>, нам нужен ID
            val result = getCurrentUserUseCase()
            val userId = result.getOrNull()?.id ?: ""

            _state.update { it.copy(currentUserId = userId) }
        }
    }
    fun loadMessage(chatId: String){
        getMessagesUseCase(chatId = chatId)
            .onStart { _state.value = _state.value.copy(isLoading = true,error = null) }
            .catch { exception->
                _state.value = _state.value.copy(isLoading = false,
                    error = exception.message ?: "Произошла ошибка загрузки сообщений")
            }
            .onEach { messages ->
                _state.value = _state.value.copy(isLoading = false, message = messages)
            }
            .launchIn(viewModelScope)
    }
     fun sendMessage(){
         val userId = _state.value.currentUserId
         if(userId.isBlank()) return
        if (messageText.isBlank()) return
        viewModelScope.launch {

            try {
                sendMessageUseCase(chatId = chatId, text = messageText, userId = userId)
                messageText = ""
            }catch (e: Exception) {
                _state.value = _state.value.copy(error = "Не удалось отправить")
            }
        }


    }
    fun handleIntent(intent: ChatDetailIntent) {
        when(intent) {
            is ChatDetailIntent.OnMessageTextChanged -> {
                messageText = intent.text
            }
            is ChatDetailIntent.OnSendClick -> {
                sendMessage()
            }

            ChatDetailIntent.LoadMessages -> TODO()
        }
    }
}