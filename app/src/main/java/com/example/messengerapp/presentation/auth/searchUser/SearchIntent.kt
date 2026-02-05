package com.example.messengerapp.presentation.auth.searchUser

sealed class SearchIntent {
    data class OnQueryChanged(val query:String): SearchIntent()
    object OnSearchClicked: SearchIntent()
    object OnUserClicked: SearchIntent()
}