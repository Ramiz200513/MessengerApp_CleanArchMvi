package com.example.messengerapp.presentation.auth.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.domain.model.User
import com.example.domain.domain.usecase.SignUpUseCase
import com.example.messengerapp.presentation.auth.RegistrationIntent.RegistrationIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state = _state.asStateFlow()

    var usernameInput by mutableStateOf("")
    var emailInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")

    fun handleIntent(intent: RegistrationIntent) {
        when (intent) {
            is RegistrationIntent.OnEmailChanged -> emailInput = intent.email
            is RegistrationIntent.OnPasswordChanged -> passwordInput = intent.password
            is RegistrationIntent.OnUsernameChanged -> usernameInput = intent.username
            is RegistrationIntent.OnSignUpClick -> signUp()
        }
    }

    private fun signUp() {
        viewModelScope.launch {

            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = signUpUseCase(
                email = emailInput,
                username = usernameInput,
                password = passwordInput
            )
            result.onSuccess { authUser->
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Ошибка регистрации"
                )
            }
        }
    }
}