package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING
}

class YunaVoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    init {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _voiceState.value = VoiceState.SPEAKING
                }

                override fun onDone(utteranceId: String?) {
                    _voiceState.value = VoiceState.IDLE
                    _audioAmplitude.value = 0f
                }

                override fun onError(utteranceId: String?) {
                    _voiceState.value = VoiceState.IDLE
                    _audioAmplitude.value = 0f
                }
            })
        }
    }

    fun startListening(languageCode: String = "auto") {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError?.invoke("Speech recognition is not available on this device.")
            return
        }

        stopSpeaking()

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = VoiceState.LISTENING
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize -2dB to 10dB to 0f..1f range
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _audioAmplitude.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = VoiceState.PROCESSING
                        _audioAmplitude.value = 0f
                    }

                    override fun onError(error: Int) {
                        _voiceState.value = VoiceState.IDLE
                        _audioAmplitude.value = 0f
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy"
                            SpeechRecognizer.ERROR_SERVER -> "Speech server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                            else -> "Recognition error: $error"
                        }
                        onError?.invoke(message)
                    }

                    override fun onResults(results: Bundle?) {
                        _voiceState.value = VoiceState.IDLE
                        _audioAmplitude.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            _recognizedText.value = text
                            onSpeechRecognized?.invoke(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let { _recognizedText.value = it }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            when (languageCode.lowercase()) {
                "bn" -> putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                "ko" -> putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                "en" -> putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                else -> putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            }
        }

        try {
            speechRecognizer?.startListening(intent)
            _voiceState.value = VoiceState.LISTENING
        } catch (e: Exception) {
            _voiceState.value = VoiceState.IDLE
            onError?.invoke("Could not start speech recognition: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // ignore
        }
        _voiceState.value = VoiceState.IDLE
        _audioAmplitude.value = 0f
    }

    fun speak(text: String, languageCode: String = "auto", speed: Float = 1.0f) {
        if (!isTtsReady || textToSpeech == null) return

        stopSpeaking()

        val locale = when (languageCode.lowercase()) {
            "bn" -> Locale.forLanguageTag("bn-BD")
            "ko" -> Locale.KOREAN
            "en" -> Locale.US
            else -> {
                // Auto-detect based on text characters
                when {
                    text.any { it in '\u0980'..'\u09FF' } -> Locale.forLanguageTag("bn-BD")
                    text.any { it in '\uAC00'..'\uD7AF' || it in '\u1100'..'\u11FF' } -> Locale.KOREAN
                    else -> Locale.US
                }
            }
        }

        textToSpeech?.apply {
            this.language = locale
            setSpeechRate(speed)
            _voiceState.value = VoiceState.SPEAKING
            // Clean markdown asterisks for smoother TTS reading
            val cleanText = text.replace("*", "").replace("#", "").replace("`", "")
            speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "yuna_utterance_${System.currentTimeMillis()}")
        }
    }

    fun stopSpeaking() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        _voiceState.value = VoiceState.IDLE
        _audioAmplitude.value = 0f
    }

    fun emergencyStop() {
        stopListening()
        stopSpeaking()
        _voiceState.value = VoiceState.IDLE
        _audioAmplitude.value = 0f
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
