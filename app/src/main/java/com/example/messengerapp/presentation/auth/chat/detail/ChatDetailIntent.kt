package com.example.messengerapp.presentation.auth.chat.detail

import android.net.Uri

sealed class ChatDetailIntent {
    data class OnMessageTextChanged(val text:String): ChatDetailIntent()
    object OnSendClick: ChatDetailIntent()
    object LoadMessages: ChatDetailIntent()
    data class OnImageSelected(val uri: Uri) : ChatDetailIntent()
    data class OnDeleteMessage(val messageId: String) : ChatDetailIntent()
}