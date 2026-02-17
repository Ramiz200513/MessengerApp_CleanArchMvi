package com.example.messengerapp.data.network

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class FcmV1Request(
    val message: FcmMessage
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class FcmMessage(
    val token: String,
    val notification: FcmNotification
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class FcmNotification(
    val title: String,
    val body: String
)