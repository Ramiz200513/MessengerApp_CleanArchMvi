package com.example.messengerapp.data.network

import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FcmTokenManager {

    private val serviceAccountJson = FcmKeyStore.SERVICE_ACCOUNT_JSON
    suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        val inputStream = serviceAccountJson.byteInputStream()
        val credentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

        credentials.refreshIfExpired()
        "Bearer ${credentials.accessToken.tokenValue}"
    }
}