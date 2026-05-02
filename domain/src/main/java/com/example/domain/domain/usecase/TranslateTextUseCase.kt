package com.example.domain.domain.usecase

import com.example.domain.domain.repository.AITranslatorRepository
import javax.inject.Inject

class TranslateTextUseCase @Inject constructor(
    private val repository: AITranslatorRepository
) {
    suspend operator fun invoke(text: String, targetLanguage: String = "Russian"): Result<String> {
        return repository.translateText(text, targetLanguage)
    }
}