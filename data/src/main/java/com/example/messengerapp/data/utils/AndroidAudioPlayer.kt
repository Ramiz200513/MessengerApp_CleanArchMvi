package com.example.messengerapp.data.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri

class AndroidAudioPlayer(
    private val context: Context
) {
    private var player: MediaPlayer? = null

    fun playFile(url: String) {
        stop()
        player = MediaPlayer().apply {
            setDataSource(url)
            prepare()
            start()
        }
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

    fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
    }
}