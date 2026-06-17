package com.example.messengerapp.presentation.auth.chat.list

import android.util.Log.e
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.domain.model.ChatWithPartner
import com.example.domain.domain.repository.ChatRepository
import com.example.domain.domain.usecase.GetChatsUseCase
import com.example.domain.domain.usecase.GetMyChatsUseCase
import com.example.domain.domain.usecase.MarkAsFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getChatsUseCase: GetMyChatsUseCase,
    private val markAsFavoriteUseCase: MarkAsFavoriteUseCase
): ViewModel() {
    private val _state = MutableStateFlow(ChatListState())
    val state = _state.asStateFlow()
    init {
        loadChats()
    }
    fun handleIntent(intent: ChatListIntent){
        when(intent){
            is ChatListIntent.ToggleFavorite -> {
                viewModelScope.launch {
                    markAsFavoriteUseCase(intent.chatId)
                }
            }
        }
    }
    //чат в ищзбранн,ое
    //рофиль чат
    //аватарки смотрет, в сети
    fun loadChats(){
        getChatsUseCase()
            .onStart {
                _state.update {it.copy(isLoading = true)}
            }
            .catch { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Что-то пошло не так"
                )
            }
            .onEach { list ->
                val sortedChats = list.sortedWith ( compareByDescending<ChatWithPartner>{ it.chat.isFavorite }
                    .thenByDescending { it.chat.lastModified }
                )

                _state.update { it.copy(isLoading = false, chats = sortedChats) }
            }
            .launchIn(viewModelScope)
    }
}