package com.example.messengerapp.presentation.auth.biometric

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionAuthManager @Inject constructor() {
    var isAppUnlocked: Boolean = false
}