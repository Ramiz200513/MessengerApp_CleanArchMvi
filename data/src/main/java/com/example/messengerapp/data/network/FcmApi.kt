package com.example.messengerapp.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface FcmApi { //
    @Headers("Content-Type: application/json")
    @POST("https://fcm.googleapis.com/v1/projects/firstmessanger-a62fb/messages:send")
    suspend fun sendNotification(
        @Header("Authorization") bearerToken: String,
        @Body request: FcmV1Request
    ): Response<Unit>
}