package com.example.messengerapp.presentation.auth.login

sealed class LoginIntent {
    data class OnEmailChanged(val email:String): LoginIntent()
    data class OnPaswwordChanged(val password:String): LoginIntent()
    object OnLoginClick: LoginIntent()
}