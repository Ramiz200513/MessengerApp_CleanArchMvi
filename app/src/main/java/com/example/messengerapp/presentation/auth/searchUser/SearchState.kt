package com.example.messengerapp.presentation.auth.searchUser

import com.example.domain.domain.model.User

data class SearchState (
    val isLoading:Boolean = false,
    val error: String? = null,
    val query:String = "",
    val foundUser: User? = null,
    val createdChatId:String? = null
)