package com.example.domain.domain.usecase

import android.net.Uri
import com.example.domain.domain.repository.ProfileRepository
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(uri: Uri): Result<String> {
        return repository.uploadAvatar(uri)
    }
}