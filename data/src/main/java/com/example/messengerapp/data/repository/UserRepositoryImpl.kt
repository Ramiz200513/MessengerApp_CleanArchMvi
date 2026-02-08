package com.example.messengerapp.data.repository

import com.example.domain.domain.model.User
import com.example.domain.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): UserRepository {
    override suspend fun searchUserByEmail(email: String): Result<User?> {
        return try {
            val querySnapshot =firestore.collection("users")
                .whereEqualTo("email",email)
                .get()
                .await()
            if(querySnapshot!=null){
                val document = querySnapshot.documents[0]
                val user = document.toObject(User::class.java)
                Result.success(user)
            }else{
                Result.success(null)
            }
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val user = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            if (user!=null){
                val user = user.toObject(User::class.java)
                Result.success(user)
            }else{
                Result.success(null)
            }
        }catch (e: Exception){
            Result.failure(e)
        }

        }
    }

