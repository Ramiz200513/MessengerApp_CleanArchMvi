package com.example.messengerapp.data.repository

import com.example.domain.domain.model.User
import com.example.domain.domain.repository.UserRepository
import com.example.messengerapp.data.local.dao.UserDao
import com.example.messengerapp.data.local.mappers.toDomain
import com.example.messengerapp.data.local.mappers.toEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val auth: FirebaseAuth
): UserRepository {
    override suspend fun searchUserByEmail(email: String): Result<User?> {
        return try {
            val querySnapshot =firestore.collection("users")
                .whereEqualTo("email",email)
                .get()
                .await()
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                val user = document.toObject(User::class.java)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val localUser = userDao.getUserById(userId)
            if (localUser!=null){
                val userDomain = localUser.toDomain()
                return Result.success(userDomain)
            }
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            if(snapshot.exists()){
                val networkUser = snapshot.toObject(User::class.java)
                if (networkUser!=null){
                    userDao.upsertUser(networkUser.toEntity())
                    Result.success(networkUser)
                }else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }//
    override suspend fun updateFcmToken(token: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No user"))
            firestore.collection("users").document(userId)
                .update("fcmToken", token)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}