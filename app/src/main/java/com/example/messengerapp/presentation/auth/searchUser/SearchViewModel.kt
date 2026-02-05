package com.example.messengerapp.presentation.auth.searchUser

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.domain.repository.ChatRepository
import com.example.domain.domain.usecase.CreateChatUseCase
import com.example.domain.domain.usecase.SearchUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUserUseCase: SearchUserUseCase,
    private val createChatUseCase: CreateChatUseCase
): ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()
    fun handleIntent(intent: SearchIntent){
        when(intent){
            is SearchIntent.OnQueryChanged -> {
                _state.update { it.copy(query = intent.query, error = null) }
            }
            is SearchIntent.OnSearchClicked -> {
                searchUser()
            }
            is SearchIntent.OnUserClicked -> {
                createChat()
            }
        }
    }
    private fun searchUser(){
        val email = _state.value.query
        if (email.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, foundUser = null) }
            searchUserUseCase(email = email).onSuccess{ user ->
                if(user !=null){
                    _state.update { it.copy(isLoading = false, foundUser = user) }
                } else {
                    // Запрос успешен, но юзера нет
                    _state.update { it.copy(isLoading = false, error = "Пользователь не найден") }
                }
            }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
    private fun createChat(){
        val user = _state.value.foundUser ?: return
        viewModelScope.launch {
            createChatUseCase(user.id)
                .onSuccess { chatId ->
                    _state.update { it.copy(isLoading = false, createdChatId = chatId) }
                }.onFailure { e->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun navigationDone() {
        _state.update { it.copy(createdChatId = null) }
    }
}