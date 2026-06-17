package com.example.messengerapp.presentation.auth.chat.detail

import android.net.Uri
import com.example.domain.domain.model.Message

sealed class ChatDetailIntent {
    data class OnMessageTextChanged(val text: String) : ChatDetailIntent()
    object OnSendClick : ChatDetailIntent()
    data class OnImageSelected(val uri: Uri) : ChatDetailIntent()
    data class OnDeleteMessage(val messageId: String) : ChatDetailIntent()
    data class OnVideoSelected(val uri: Uri) : ChatDetailIntent()
    data class OnVoiceRecorded(val uri: Uri, val duration: Int, val text: String?) : ChatDetailIntent()
    data class OnToggleReaction(val messageId: String, val emoji: String) : ChatDetailIntent()
    object OnBiometricSuccess : ChatDetailIntent()
    data class OnSwipeToReply(val message: Message) : ChatDetailIntent()
    object OnCancelReply : ChatDetailIntent()
    object OnToggleSearch : ChatDetailIntent()
    data class OnSearchQueryChanged(val query: String) : ChatDetailIntent()
    data class OnTranscribeClick(val messageId: String, val voiceUrl: String) : ChatDetailIntent()
    object OnGenerateSummaryClick : ChatDetailIntent()
    data class OnTranslateMessageClick(val messageId: String, val text: String) : ChatDetailIntent()
    object OnCloseSummary : ChatDetailIntent()
    data class OnTranslateImageClick(val messageId: String, val imageUrl: String) : ChatDetailIntent()
    data class OnPinMessage(val messageId: String, val pin: Boolean) : ChatDetailIntent() // НОВОЕ
}