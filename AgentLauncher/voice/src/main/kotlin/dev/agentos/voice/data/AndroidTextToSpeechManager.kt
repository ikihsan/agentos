package dev.agentos.voice.data

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.agentos.core.common.AgentLogger
import dev.agentos.core.common.DispatcherProvider
import dev.agentos.core.common.Result
import dev.agentos.voice.domain.TextToSpeechManager
import dev.agentos.voice.domain.TtsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Android TextToSpeech implementation.
 */
@Singleton
class AndroidTextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider
) : TextToSpeechManager {

    private val _state = MutableStateFlow<TtsState>(TtsState.Initializing)
    override val state: StateFlow<TtsState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var speechRate = 1.0f
    private var pitch = 1.0f

    init {
        initializeTts()
    }

    private fun initializeTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale.getDefault())
                    isInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                            result != TextToSpeech.LANG_NOT_SUPPORTED
                    
                    if (isInitialized) {
                        _state.value = TtsState.Idle
                        AgentLogger.i(AgentLogger.TAG_VOICE, "TTS initialized")
                    } else {
                        _state.value = TtsState.Error("Language not supported")
                    }
                }
            } else {
                _state.value = TtsState.Error("TTS initialization failed")
                AgentLogger.e(AgentLogger.TAG_VOICE, "TTS initialization failed")
            }
        }
    }

    override suspend fun speak(text: String): Result<Unit> = withContext(dispatchers.main) {
        if (!isInitialized) {
            return@withContext Result.error("TTS not initialized")
        }

        suspendCancellableCoroutine { continuation ->
            val utteranceId = UUID.randomUUID().toString()
            
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _state.value = TtsState.Speaking
                }

                override fun onDone(utteranceId: String?) {
                    _state.value = TtsState.Idle
                    if (continuation.isActive) {
                        continuation.resume(Result.success(Unit))
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _state.value = TtsState.Error("Speech error")
                    if (continuation.isActive) {
                        continuation.resume(Result.error("Speech error"))
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _state.value = TtsState.Error("Speech error: $errorCode")
                    if (continuation.isActive) {
                        continuation.resume(Result.error("Speech error: $errorCode"))
                    }
                }
            })

            tts?.setSpeechRate(speechRate)
            tts?.setPitch(pitch)
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
            
            AgentLogger.d(AgentLogger.TAG_VOICE, "Speaking: $text")

            continuation.invokeOnCancellation {
                tts?.stop()
                _state.value = TtsState.Idle
            }
        }
    }

    override suspend fun speakImmediate(text: String): Result<Unit> = withContext(dispatchers.main) {
        if (!isInitialized) {
            return@withContext Result.error("TTS not initialized")
        }

        // Stop any current speech
        tts?.stop()
        
        // Speak with flush queue
        suspendCancellableCoroutine { continuation ->
            val utteranceId = UUID.randomUUID().toString()
            
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _state.value = TtsState.Speaking
                }

                override fun onDone(utteranceId: String?) {
                    _state.value = TtsState.Idle
                    if (continuation.isActive) {
                        continuation.resume(Result.success(Unit))
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _state.value = TtsState.Error("Speech error")
                    if (continuation.isActive) {
                        continuation.resume(Result.error("Speech error"))
                    }
                }
            })

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

            continuation.invokeOnCancellation {
                tts?.stop()
                _state.value = TtsState.Idle
            }
        }
    }

    override fun stop() {
        tts?.stop()
        _state.value = TtsState.Idle
    }

    override fun isAvailable(): Boolean = isInitialized

    override fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
    }

    override fun setPitch(pitch: Float) {
        this.pitch = pitch.coerceIn(0.5f, 2.0f)
    }
}
