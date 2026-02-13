package com.example.messengerapp.data.repository

import com.example.domain.domain.model.User
import com.example.domain.domain.repository.AuthRepository
import com.example.messengerapp.data.local.AppDatabase
import com.example.messengerapp.data.local.dao.UserDao
import com.example.messengerapp.data.local.mappers.toDomain
import com.example.messengerapp.data.local.mappers.toEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val db: AppDatabase,
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email,password).await()
            val uid = authResult.user?.uid
            if(uid!=null){
                val snapshot = firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()
                val networkUser = snapshot.toObject(User::class.java)

                if (networkUser != null) {
                    userDao.upsertUser(networkUser.toEntity())
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String, username: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("User creation failed")

            val newUser = User(
                id = uid,
                username = username,
                email = email,
                photoUrl = null,
                isOnline = true
            )
            firestore.collection("users").document(uid)
                .set(newUser)
                .await()
            userDao.upsertUser(newUser.toEntity())

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            return Result.failure(Exception("User not logged in"))
        }

        return try {
            val localUserEntity = userDao.getUserById(currentUserId)
            if (localUserEntity != null) {

                return Result.success(localUserEntity.toDomain())
            }
            val snapshot = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val networkUser = snapshot.toObject(User::class.java)

            if (networkUser != null) {
                userDao.upsertUser(networkUser.toEntity())
                Result.success(networkUser)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            db.clearAllTablesData()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}