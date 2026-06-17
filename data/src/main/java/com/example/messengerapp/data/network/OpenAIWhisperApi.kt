package com.example.messengerapp.data.network

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OpenAIWhisperApi {
    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody = "whisper-1".toRequestBody(),
        @Header("Authorization") token: String
    ): WhisperResponse
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class WhisperResponse(
    val text: String
    // your other fields here
)