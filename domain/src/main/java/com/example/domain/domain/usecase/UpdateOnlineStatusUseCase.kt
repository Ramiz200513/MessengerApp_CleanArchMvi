package com.example.domain.domain.usecase

import com.example.domain.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateOnlineStatusUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(isOnline: Boolean) {
        repository.updateOnlineStatus(isOnline)
    }
}