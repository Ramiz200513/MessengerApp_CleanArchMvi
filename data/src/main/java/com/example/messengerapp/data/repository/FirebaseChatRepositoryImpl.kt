package com.example.messengerapp.data.repository


import android.content.Context
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
import com.example.messengerapp.data.network.OpenAIWhisperApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject
import com.example.messengerapp.data.BuildConfig

class FirebaseChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val storage: FirebaseStorage,
    private val fcmApi: FcmApi,
    private val whisperApi: OpenAIWhisperApi,
    @ApplicationContext private val context: Context
) : ChatRepository {

    override fun searchMessages(
        chatId: String,
        query: String
    ): Flow<List<Message>> {
        val ftsQuery = "$query*"
        return messageDao.searchMessagesInChat(chatId, ftsQuery).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    override suspend fun transcribeVoiceMessage(
        chatId: String,
        messageId: String,
        audioUrl: String
    ): Result<Unit> = withContext(Dispatchers.IO) { // Обязательно уходим в фоновый поток для работы с файлами и сетью
        try {
            val messageRef = firestore.collection("chats").document(chatId)
                .collection("messages").document(messageId)

            // 1. Ставим флаг загрузки в Firestore (чтобы у всех пользователей UI показал спиннер/индикатор)
            messageRef.update("isTranscribing", true).await()

            // 2. Скачиваем аудиофайл по URL во временный кэш
            val tempFile = File.createTempFile("voice_${messageId}", ".m4a", context.cacheDir)
            java.net.URL(audioUrl).openStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val requestFile = tempFile.asRequestBody("audio/mp4".toMediaTypeOrNull()) // m4a обычно имеет MIME тип audio/mp4
            val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

            val token = "Bearer ${BuildConfig.OPENAI_API_KEY}"
            val response = whisperApi.transcribeAudio(file = filePart, token = token)

            messageRef.update(
                mapOf(
                    "text" to response.text,
                    "isTranscribing" to false
                )
            ).await()

            // 6. Подчищаем за собой (удаляем временный файл)
            tempFile.delete()

            Result.success(Unit)
        } catch (e: Exception) {
            // В случае любой ошибки (сеть, OpenAI упал, прав нет) обязательно снимаем флаг загрузки,
            // чтобы сообщение не "зависло" в состоянии транскрибации навсегда
            firestore.collection("chats").document(chatId)
                .collection("messages").document(messageId)
                .update("isTranscribing", false)
                .await()

            Result.failure(e)
        }
    }
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
                val body = when {
                    !message.text.isNullOrBlank() -> message.text
                    message.imageUrl != null -> "Отправил фото"
                    message.videoUrl != null -> "Отправил видео"
                    message.voiceUrl != null -> "Голосовое сообщение"
                    else -> "Новое сообщение"
                }
                val request = FcmV1Request(
                    message = FcmMessage(
                        token = receiverToken,
                        notification = FcmNotification(
                            title = "Новое сообщение",
                            body = body!!
                        )
                    )
                )

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

            val participants = chatDoc.get("participants") as? List<*>
            val participantsIds = participants?.filterIsInstance<String>() ?: return null
            val receiverId = participantsIds.firstOrNull { it != myId } ?: return null
            val userDoc = firestore.collection("users").document(receiverId).get().await()
            userDoc.getString("fcmToken")
        } catch (_:Exception) {
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
                                doc.toObject(Chat::class.java)?.copy(
                                    id = doc.id,
                                    isFavorite = doc.getBoolean("isFavorite") ?: false
                                )
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
                            videoUrl = doc.getString("videoUrl"),
                            voiceUrl = doc.getString("voiceUrl"),
                            voiceDuration = doc.getLong("voiceDuration")?.toInt(),
                            reactions = (doc.get("reactions") as? Map<String, List<String>>) ?: emptyMap(),
                            senderId = doc.getString("senderId") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isRead = doc.getBoolean("isRead") ?: false,
                            replyToMessageId = doc.getString("replyToMessageId"),
                            replyToMessageText = doc.getString("replyToMessageText"),
                            isPinned = doc.getBoolean("isPinned") ?: false
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
                participantsCsv = listOf(currentUserId,otherUserId).joinToString(","),
                isFavorite = false
            )
            chatDao.upsertChats(listOf(newChatEntity))
            Result.success(documentReference.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendVideoMessage(
        chatId: String,
        uri: Uri
    ): Result<Unit> {
        return try {
            val currentId = auth.currentUser?.uid ?: throw Exception("Юзер не вошел в аккаунт!")
            val videoId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("chats/$chatId/videos/$videoId.mp4")
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            val message = Message(
                id = videoId,
                text = "",
                videoUrl = downloadUrl,
                senderId = currentId,
                timestamp = System.currentTimeMillis()
            )
            sendMessage(chatId, message)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
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
                                doc.toObject(Chat::class.java)?.copy(
                                    id = doc.id,
                                    isFavorite = doc.getBoolean("isFavorite") ?: false
                                )
                            }

                            launch {
                                chatDao.upsertChats(chats.map { it.toEntity() })
                                chats.forEach { chat ->
                                    try {
                                        val msgsSnapshot = firestore.collection("chats")
                                            .document(chat.id)
                                            .collection("messages")
                                            .orderBy("timestamp", Query.Direction.DESCENDING)
                                            .limit(1)
                                            .get()
                                            .await()

                                        val msgDoc = msgsSnapshot.documents.firstOrNull()
                                        if (msgDoc != null) {
                                            val lastMessage = Message(
                                                id = msgDoc.id,
                                                text = msgDoc.getString("text"),
                                                imageUrl = msgDoc.getString("imageUrl"),
                                                videoUrl = msgDoc.getString("videoUrl"),
                                                voiceUrl = msgDoc.getString("voiceUrl"),
                                                voiceDuration = msgDoc.getLong("voiceDuration")?.toInt(),
                                                senderId = msgDoc.getString("senderId") ?: "",
                                                timestamp = msgDoc.getLong("timestamp") ?: 0L,
                                                isRead = msgDoc.getBoolean("isRead") ?: false,
                                                replyToMessageId = msgDoc.getString("replyToMessageId"),
                                                replyToMessageText = msgDoc.getString("replyToMessageText")
                                            )

                                            val messageEntity = lastMessage.toEntity().copy(chatId = chat.id)
                                            messageDao.upsertMessages(listOf(messageEntity))
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
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
    override suspend fun pinMessage(chatId: String, messageId: String, pin: Boolean): Result<Unit> {
        return try {
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .update("isPinned", pin)
                .await()

            val pinnedMessageId = if (pin) messageId else null
            firestore.collection("chats")
                .document(chatId)
                .update("pinnedMessageId", pinnedMessageId)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun sendVoiceMessage(chatId: String, uri: Uri, duration: Int, text: String?): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val voiceId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("chats/$chatId/voices/$voiceId.m4a")
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            val message = Message(
                id = voiceId,
                text = text,
                voiceUrl = downloadUrl,
                voiceDuration = duration,
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
    override suspend fun toggleReaction(chatId: String, messageId: String, emoji: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val messageRef = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)

            val doc = messageRef.get().await()
            val reactions = (doc.get("reactions") as? Map<String, List<String>>) ?: emptyMap()
            val users = reactions[emoji] ?: emptyList()

            if (users.contains(currentUserId)) {
                messageRef.update("reactions.$emoji", FieldValue.arrayRemove(currentUserId)).await()
            } else {
                messageRef.update("reactions.$emoji", FieldValue.arrayUnion(currentUserId)).await()
            }
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

    override suspend fun markAsFavorite(chatId: String) {
        try {
            val chat = chatDao.getChatOneShot(chatId) ?: return
            val newStatus = !chat.isFavorite


            chatDao.updateFavoriteStatus(chatId, newStatus)
            firestore.collection("chats")
                .document(chatId)
                .set(mapOf("isFavorite" to newStatus), SetOptions.merge())
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}