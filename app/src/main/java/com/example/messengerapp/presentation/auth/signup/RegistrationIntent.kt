package com.example.messengerapp.presentation.auth.RegistrationIntent


sealed class RegistrationIntent {
    data class OnEmailChanged(val email:String): RegistrationIntent()
    data class OnPasswordChanged(val password:String): RegistrationIntent()
    data class OnUsernameChanged(val username:String): RegistrationIntent()
    object OnSignUpClick: RegistrationIntent()
}