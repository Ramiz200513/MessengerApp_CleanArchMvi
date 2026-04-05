package com.example.messengerapp.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromReactionsMap(value: Map<String, List<String>>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toReactionsMap(value: String): Map<String, List<String>> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}