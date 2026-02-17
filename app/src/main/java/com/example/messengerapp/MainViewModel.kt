package com.example.messengerapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.domain.usecase.GetCurrentUserUseCase
import com.example.messengerapp.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel@Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel(){
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()
    init {
        checkSession()
    }
    fun checkSession(){
        viewModelScope.launch {
            val result = getCurrentUserUseCase()
            result.onSuccess {
                _startDestination.value = Screen.ChatList.route
            }.onFailure {
                _startDestination.value = Screen.Login.route
            }
        }
    }
}