package com.example.messengerapp.presentation.auth.profile

import com.example.domain.domain.model.User

data class ProfileState (
    val isLoading:Boolean = false,
    val error:String? = null,
    val user: User? = null,
    val isSignedOut:Boolean = false,
    val isEditing:Boolean = false
)