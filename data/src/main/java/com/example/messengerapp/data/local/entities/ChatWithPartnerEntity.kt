package com.example.messengerapp.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity
data class ChatWithPartnerEntity (
    @Embedded val chat: ChatEntity,
    @Relation(
        parentColumn = "lastModified",
        entityColumn = "id"
    )
    val partner: UserEntity? = null
)