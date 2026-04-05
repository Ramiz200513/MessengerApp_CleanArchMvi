package com.example.messengerapp.presentation.auth.chat.detail

import android.net.Uri

sealed class ChatDetailIntent {
    data class OnMessageTextChanged(val text:String): ChatDetailIntent()
    object OnSendClick: ChatDetailIntent()
    object LoadMessages: ChatDetailIntent()
    data class OnImageSelected(val uri: Uri) : ChatDetailIntent()
    data class OnDeleteMessage(val messageId: String) : ChatDetailIntent()
    data class OnVideoSelected(val uri:Uri): ChatDetailIntent()
    data class OnVoiceRecorded(val uri: Uri, val duration: Int) : ChatDetailIntent()
    data class OnToggleReaction(val messageId: String, val emoji: String) : ChatDetailIntent()
}