package com.example.messengerapp.presentation.auth.signup

data class RegistrationState(
    val isLoading:Boolean = false,
    val error:String? = null,
    val isSuccess:Boolean = false,
)