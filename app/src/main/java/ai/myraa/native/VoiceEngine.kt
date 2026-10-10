package ai.myraa.native

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/** On-device speech recognition + text-to-speech. Hindi, Hinglish and English. */
class VoiceEngine(
    private val ctx: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private val main = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            AppState.listening.value = true
            AppState.status.value = "Listening…"
        }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { AppState.listening.value = false }
        override fun onError(error: Int) {
            AppState.listening.value = false
            AppState.status.value = "Ready"
            onError(speechError(error))
        }
        override fun onResults(results: Bundle?) {
            AppState.listening.value = false
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            if (text.isNotBlank()) onResult(text) else AppState.status.value = "Ready"
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun init() {
        main.post {
            try {
                recognizer = SpeechRecognizer.createSpeechRecognizer(ctx)
                recognizer?.setRecognitionListener(listener)
            } catch (_: Exception) {}
        }
        try {
            tts = TextToSpeech(ctx) { status ->
                ttsReady = status == TextToSpeech.SUCCESS
                if (ttsReady) {
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            AppState.anim.value = Anim.TALKING
                            AppState.status.value = "Speaking"
                        }
                        override fun onDone(utteranceId: String?) {
                            AppState.anim.value = Anim.IDLE
                            AppState.status.value = "Ready"
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            AppState.anim.value = Anim.IDLE
                        }
                    })
                }
            }
        } catch (_: Exception) {}
    }

    fun listen(langTag: String) {
        main.post {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            try {
                recognizer?.startListening(i)
            } catch (e: Exception) {
                onError(e.message ?: "Microphone error")
            }
        }
    }

    fun stop() {
        main.post { try { recognizer?.stopListening() } catch (_: Exception) {} }
    }

    fun speak(text: String, langTag: String) {
        if (!ttsReady) return
        try {
            tts?.language = Locale.forLanguageTag(langTag)
        } catch (_: Exception) {}
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "myraa")
    }

    fun release() {
        try { recognizer?.destroy() } catch (_: Exception) {}
        try { tts?.stop(); tts?.shutdown() } catch (_: Exception) {}
    }

    private fun speechError(code: Int) = when (code) {
        SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that, try again"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission needed"
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network problem during recognition"
        else -> "Microphone error ($code)"
    }
}
