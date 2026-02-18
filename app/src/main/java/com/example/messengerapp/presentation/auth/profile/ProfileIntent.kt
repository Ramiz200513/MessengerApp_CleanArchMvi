package com.example.messengerapp.presentation.auth.profile

import android.net.Uri

sealed class ProfileIntent {
    object OnSignOutClicked: ProfileIntent()
    object LoadProfile: ProfileIntent()
    data class OnNameChanged(val newName:String): ProfileIntent()
    object OnSaveProfile: ProfileIntent()
    data class OnAvatarChanged(val uri: Uri): ProfileIntent()
}