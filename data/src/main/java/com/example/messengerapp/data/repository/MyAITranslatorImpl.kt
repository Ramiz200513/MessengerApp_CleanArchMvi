package com.example.messengerapp.data.repository

import com.example.domain.domain.repository.AITranslatorRepository
import com.example.messengerapp.data.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class MyAITranslatorImpl @Inject constructor() : AITranslatorRepository {
    
    override suspend fun translateText(text: String, targetLanguage: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GROQ_API_KEY
            val url = URL("https://api.groq.com/openai/v1/chat/completions")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true

            val prompt = "Translate the following text to $targetLanguage. Provide ONLY the translated text without any explanations or extra characters:\n\n$text"
            
            val jsonBody = JSONObject().apply {
                put("model", "llama-3.3-70b-versatile")
                val messages = org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
                put("messages", messages)
                put("max_tokens", 1000)
            }

            OutputStreamWriter(connection.outputStream).use { it.write(jsonBody.toString()) }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonObject = JSONObject(response)
                val translatedText = jsonObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
                Result.success(translatedText)
            } else {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                Result.failure(Exception("HTTP ${connection.responseCode}: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}