package com.example.domain.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Chat (
    val id:String= "",
    val name:String = "",
    val description:String? = null,
    val participants: List<String> = emptyList(),
)