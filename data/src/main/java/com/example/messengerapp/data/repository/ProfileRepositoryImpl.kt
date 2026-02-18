package com.example.messengerapp.data.repository

import android.net.Uri
import com.example.domain.domain.model.User
import com.example.domain.domain.repository.ProfileRepository
import com.example.messengerapp.data.local.dao.UserDao
import com.example.messengerapp.data.local.mappers.toEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val userDao: UserDao
): ProfileRepository {
    override suspend fun uploadAvatar(imageUri: Uri): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User Not Found")
            val storageRef = storage.reference.child("avatars/$uid.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            firestore.collection("users").document(uid).update("photoUrl", downloadUrl).await()
            val currentUser = userDao.getUserById(uid)
            if (currentUser != null) {
                val updatedUser = currentUser.copy(photoUrl = downloadUrl)
                userDao.upsertUser(updatedUser)
            }
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveProfileEdit(user: User): Result<User> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            userDao.upsertUser(user.toEntity())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}