package com.example.domain.domain.usecase

import com.example.domain.domain.repository.UserRepository
import javax.inject.Inject

class UpdateFcmTokenUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(token: String) {
        repository.updateFcmToken(token)
    }
}//