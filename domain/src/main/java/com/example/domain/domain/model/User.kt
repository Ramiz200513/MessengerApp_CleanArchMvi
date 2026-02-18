package com.example.domain.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    var online: Boolean = false,
    val fcmToken: String? = null
)