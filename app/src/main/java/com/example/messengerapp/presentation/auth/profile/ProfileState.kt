package com.example.messengerapp.presentation.auth.profile

import android.net.Uri
import androidx.compose.ui.text.LinkAnnotation
import com.example.domain.domain.model.User
import java.net.URL


data class ProfileState (
    val isLoading:Boolean = false,
    val error:String? = null,
    val user: User? = null,
    val isSignedOut:Boolean = false,
    val isEditing:Boolean = false,
    val photoUrl: String? = null


)