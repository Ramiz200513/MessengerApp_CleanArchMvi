package com.example.messengerapp.data.repository

import com.example.domain.domain.model.Chat
import com.example.domain.domain.model.Message
import com.example.domain.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ChatRepository {
    override suspend fun sendMessage(chatId: String, message: Message){
        val messageToSend = message.copy(
            timestamp = System.currentTimeMillis()
        )
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(message.id)
            .set(messageToSend)
            .await()
        firestore.collection("chats").document(chatId)
            .update("lastModified", System.currentTimeMillis())
    }

    override fun getChats(): Flow<List<Chat>> {
        return callbackFlow {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                close(Exception("User not logged in"))
                return@callbackFlow
            }
            val query = firestore.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .orderBy("lastModified", Query.Direction.DESCENDING)
            val listener = query.addSnapshotListener { snapshot,error ->
                if(error != null){
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot!=null){
                    val chats = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Chat::class.java)?.copy(id = doc.id)
                    }
                    trySend(chats)
                }
            }
        awaitClose {
            listener.remove()
        }
        }
    }

    override fun getMessages(chatId: String): Flow<List<Message>> {
        return callbackFlow {
            val query = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    trySend(messages)
                }
            }
            awaitClose {
                listener.remove()
            }
        }
    }

    override suspend fun createChat(otherUserId: String): Result<String> {
        return try {
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
}