package com.example.messengerapp.presentation.auth.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.domain.usecase.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase
): ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()
    var emailInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")
    fun handleIntent(intent: LoginIntent) {
        when(intent) {
            is LoginIntent.OnEmailChanged -> emailInput = intent.email
            is LoginIntent.OnPaswwordChanged -> passwordInput = intent.password
            is LoginIntent.OnLoginClick -> login()
        }
    }
    private fun login() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = signInUseCase(emailInput, passwordInput)
            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Unknown error"
                )
            }
        }
    }
}