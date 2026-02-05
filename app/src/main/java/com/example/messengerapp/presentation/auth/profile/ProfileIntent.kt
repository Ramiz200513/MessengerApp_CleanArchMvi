package com.example.messengerapp.presentation.auth.profile

sealed class ProfileIntent {
    object OnSignOutClicked: ProfileIntent()
    object LoadProfile: ProfileIntent()
    data class OnNameChanged(val newName:String): ProfileIntent()
    object OnSaveProfile: ProfileIntent()
    data class OnAvatarChanged(val url: String): ProfileIntent()
}