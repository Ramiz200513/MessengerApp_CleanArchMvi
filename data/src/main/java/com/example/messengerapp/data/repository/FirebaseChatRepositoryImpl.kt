package com.example.messengerapp.data.repository

import android.net.Uri
import android.util.Log
import com.example.domain.domain.model.Chat
import com.example.domain.domain.model.ChatWithMessages
import com.example.domain.domain.model.Message
import com.example.domain.domain.repository.ChatRepository
import com.example.messengerapp.data.local.dao.ChatDao
import com.example.messengerapp.data.local.dao.MessageDao
import com.example.messengerapp.data.local.entities.ChatEntity
import com.example.messengerapp.data.local.mappers.toDomain
import com.example.messengerapp.data.local.mappers.toEntity
import com.example.messengerapp.data.network.FcmApi
import com.example.messengerapp.data.network.FcmMessage
import com.example.messengerapp.data.network.FcmNotification
import com.example.messengerapp.data.network.FcmTokenManager
import com.example.messengerapp.data.network.FcmV1Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirebaseChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val storage: FirebaseStorage,
    private val fcmApi: FcmApi
) : ChatRepository {
    override suspend fun sendMessage(chatId: String, message: Message) {
        val currentUserId = auth.currentUser?.uid ?: return

        val messageToSend = message.copy(
            timestamp = System.currentTimeMillis()
        )

        val entity = messageToSend.toEntity().copy(chatId = chatId)
        messageDao.upsertMessages(listOf(entity))

        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(message.id)
            .set(messageToSend)
            .await()

        firestore.collection("chats").document(chatId)
            .update("lastModified", System.currentTimeMillis())

        try {
            val receiverToken = fetchReceiverToken(chatId, currentUserId)
            if (!receiverToken.isNullOrBlank()) {
                val accessToken = FcmTokenManager.getAccessToken()
                val request = FcmV1Request(
                    message = FcmMessage(
                        token = receiverToken,
                        notification = FcmNotification(
                            title = "Новое сообщение",
                            body = message.text ?: "Отправил фото"
                        )
                    )
                )

                // 3. Отправляем
                val response = fcmApi.sendNotification(accessToken, request)

                if (!response.isSuccessful) {
                    Log.e("FCM_V1", "Error: ${response.code()} ${response.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e("FCM_V1", "Failed to send notification", e)
        }
    }
    private suspend fun fetchReceiverToken(chatId: String, myId: String): String? {
        return try {
            val chatDoc = firestore.collection("chats").document(chatId).get().await()

            // Безопасно достаем список участников
            val participants = chatDoc.get("participants") as? List<*>
            val participantsIds = participants?.filterIsInstance<String>() ?: return null

            // Находим ID оппонента
            val receiverId = participantsIds.firstOrNull { it != myId } ?: return null

            // Достаем его токен
            val userDoc = firestore.collection("users").document(receiverId).get().await()

            // Возвращаем строку (она может быть null, если поля нет в базе)
            userDoc.getString("fcmToken")
        } catch (e: Exception) {
            null
        }
    }
    override fun getChats(): Flow<List<Chat>> {
        return channelFlow {
            val currentUserId = auth.currentUser?.uid
            val localDataJob = launch {
                chatDao.getChats().collect { entities ->
                    send(entities.map { it.toDomain() })
                }
            }
            val listenerRegistration = if (currentUserId != null) {
                firestore.collection("chats")
                    .whereArrayContains("participants", currentUserId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val chats = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(Chat::class.java)?.copy(id = doc.id)
                            }
                            launch {
                                chatDao.upsertChats(chats.map { it.toEntity() })
                            }
                        }
                    }
            } else {
                null
            }

            awaitClose {
                listenerRegistration?.remove()
                localDataJob.cancel()
            }
        }
    }




    override fun getMessages(chatId: String): Flow<List<Message>> {
        return channelFlow {
            val localJob = launch {
                messageDao.getMessagesByChatId(chatId).collect { entities ->
                    send(entities.map { it.toDomain() })
                }
            }
            val query = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.documents.map { doc ->
                        Message(
                            id = doc.id,
                            text = doc.getString("text"),
                            imageUrl = doc.getString("imageUrl"),
                            senderId = doc.getString("senderId") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    }
                    val entities = messages.map { message ->
                        message.toEntity().copy(chatId = chatId)
                    }
                    launch {
                        messageDao.upsertMessages(entities)
                    }
                }
            }
            awaitClose {
                listener.remove()
                localJob.cancel()
            }
        }
    }

    override suspend fun createChat(otherUserId: String): Result<String> {
        return try {
            val localChats = chatDao.getChatsOneShot()
            val existingChats = localChats.find { chatEntity ->
                chatEntity.participantsCsv.contains(otherUserId)
            }
            if (existingChats != null) {
                return Result.success(existingChats.id)
            }
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                return Result.failure(Exception("User not logged in"))
            }
            val querySnapshot = firestore.collection("chats")
                .whereArrayContains("participants",currentUserId)
                .get()
                .await()
            val existingChat = querySnapshot.documents.find { document ->
                val chat = document.toObject(Chat::class.java)
                chat?.participants?.contains(otherUserId) == true
            }
            if(existingChat!=null){
                return Result.success(existingChat.id)
            }

            val chatData = hashMapOf(
                "participants" to listOf(currentUserId,otherUserId),
                "lastModified" to System.currentTimeMillis(),
                "typing" to emptyMap<String,Boolean>()
            )

            val documentReference = firestore.collection("chats")
                .add(chatData)
                .await()
            val newChatId = documentReference.id
            val newChatEntity = ChatEntity(
                id = newChatId,
                lastModified = System.currentTimeMillis(),
                participantsCsv = listOf(currentUserId,otherUserId).joinToString(",")
            )
            chatDao.upsertChats(listOf(newChatEntity))
            Result.success(documentReference.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setTypingStatus(chatId: String, isTyping: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("chats")
                .document(chatId)
                .update("typing.$currentUserId", isTyping)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun observeChat(chatId: String): Flow<Chat> {
        return callbackFlow {
            val listener = firestore.collection("chats")
                .document(chatId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val chat = snapshot.toObject(Chat::class.java)?.copy(id = snapshot.id)
                        if (chat != null) {
                            trySend(chat)
                        }
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    override fun getChatsWithMessages(): Flow<List<ChatWithMessages>> {
        return channelFlow {
            val currentUserId = auth.currentUser?.uid

            val localJob = launch {
                chatDao.getChatsWithLastMessages().collect { entitiesList ->
                    val domainList = entitiesList.map { entity ->
                        ChatWithMessages(
                            chat = entity.chat.toDomain(),
                            messages = entity.messages.map { it.toDomain() }
                        )
                    }
                    send(domainList)
                }
            }

            val listenerRegistration = if (currentUserId != null) {
                firestore.collection("chats")
                    .whereArrayContains("participants", currentUserId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener

                        if (snapshot != null) {
                            val chats = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(Chat::class.java)?.copy(id = doc.id)
                            }
                            launch {
                                chatDao.upsertChats(chats.map { it.toEntity() })
                            }
                        }
                    }
            } else {
                null
            }

            awaitClose {
                listenerRegistration?.remove()
                localJob.cancel()
            }
        }
    }

    override suspend fun sendImageMessage(
        chatId: String,
        image: Uri
    ): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val imageId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("chats/$chatId/images/$imageId.jpg")
            storageRef.putFile(image).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            val message = Message(
                id = imageId,
                text = "",
                imageUrl = downloadUrl,
                senderId = currentUserId,
                timestamp = System.currentTimeMillis()
            )
            sendMessage(chatId, message)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteMessage(
        chatId: String,
        messageId: String
    ): Result<Unit> {
        return try {
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .delete()
                .await()
            messageDao.deleteMessageById(messageId)
            Result.success(Unit)
        }catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMessageAsRead(chatId: String, messageId: String) {
        try {
            messageDao.markAsRead(messageId)
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .update("isRead", true)
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}