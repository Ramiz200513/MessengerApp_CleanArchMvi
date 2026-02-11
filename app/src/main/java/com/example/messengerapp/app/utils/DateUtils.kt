package com.example.messengerapp.app.utils

import androidx.compose.material3.rememberTimePickerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatTime(timeStamp:Long):String{
        val date = Date(timeStamp)
        val format = SimpleDateFormat("HH:mm",Locale.getDefault())
        return format.format(date)
    }
    fun formatDate(timeStamp: Long): String{
        val date = Date(timeStamp)
        val format = SimpleDateFormat("dd MMM", Locale.getDefault())
        return format.format(date)
    }
}