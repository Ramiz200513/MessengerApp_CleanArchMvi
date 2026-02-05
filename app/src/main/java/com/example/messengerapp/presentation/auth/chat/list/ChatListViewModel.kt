package com.example.messengerapp.presentation.auth.chat.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.domain.repository.ChatRepository
import com.example.domain.domain.usecase.GetChatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getChatsUseCase: GetChatsUseCase
): ViewModel() {
    private val _state = MutableStateFlow<ChatListState>(ChatListState())
    val state = _state.asStateFlow()
    init {
        loadChats()
    }
    fun loadChats(){
        getChatsUseCase()
            .onStart {
                _state.value = _state.value.copy(isLoading = true, error = null)
            }
            .catch { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Что-то пошло не так" )}
            .onEach { list->
                _state.value = _state.value.copy(isLoading = false,
                    chats = list)
            }
            .launchIn(viewModelScope)
    }
}