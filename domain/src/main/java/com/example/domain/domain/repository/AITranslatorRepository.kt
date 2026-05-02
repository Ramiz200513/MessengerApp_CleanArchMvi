package com.example.domain.domain.repository

interface AITranslatorRepository {
    suspend fun translateText(text: String, targetLanguage: String): Result<String>
}