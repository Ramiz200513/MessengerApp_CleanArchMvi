package com.example.messengerapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.domain.usecase.GetCurrentUserUseCase
import com.example.domain.domain.usecase.UpdateFcmTokenUseCase
import com.example.messengerapp.navigation.Screen
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel@Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase
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
    fun fetchAndSaveFcmToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task->
            if (!task.isSuccessful) return@addOnCompleteListener

            val token = task.result
            viewModelScope.launch {
                updateFcmTokenUseCase(token)
            }
        }
    }
}