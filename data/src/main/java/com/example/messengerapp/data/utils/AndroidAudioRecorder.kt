package com.example.messengerapp.data.utils

import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(
    private val context: Context
) {
    private var recorder: MediaRecorder? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizedText = "" // Тут будем копить текст
    private var isRecording = false

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    fun start(outputFile: File, onTextRecognized: (String) -> Unit) {
        if (isRecording) return
        isRecording = true
        recognizedText = ""

        // 1. Настраиваем и запускаем MediaRecorder (для файла)
        try {
            createRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(FileOutputStream(outputFile).fd)
                prepare()
                start()
                recorder = this
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Ошибка запуска MediaRecorder", e)
            isRecording = false
            return
        }

        // 2. Настраиваем и запускаем SpeechRecognizer (для текста)
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        Log.e("AudioRecorder", "SpeechRecognizer Error: $error")
                        // Если произошла ошибка распознавания, просто отдаем пустую строку (или то, что успели распознать)
                        if (isRecording) {
                            onTextRecognized(recognizedText)
                        }
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            recognizedText = matches[0] // Берем самый точный вариант
                        }
                        if (isRecording) {
                            onTextRecognized(recognizedText)
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        // Если хочешь выводить текст "на лету" прямо во время записи, можно использовать этот метод
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                // Можно раскомментировать нижнюю строку, чтобы принудительно поставить русский
                // putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            // Важно запускать на главном потоке!
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                speechRecognizer?.startListening(intent)
            }
        } else {
            Log.e("AudioRecorder", "SpeechRecognizer не доступен на этом устройстве")
        }
    }

    fun stop() {
        if (!isRecording) return
        isRecording = false

        try {
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
            recorder = null
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Ошибка остановки MediaRecorder", e)
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                speechRecognizer?.stopListening() // Это спровоцирует вызов onResults
                // ВАЖНО: Мы не вызываем destroy() сразу, иначе onResults не успеет отработать.
                // SpeechRecognizer нужно очищать позже, например, в onCleared() ViewModel,
                // но для простоты и избежания утечек можно поставить небольшую задержку:
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    speechRecognizer?.destroy()
                    speechRecognizer = null
                }, 500)
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Ошибка остановки SpeechRecognizer", e)
            }
        }
    }
}