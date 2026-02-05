package dev.agentos.voice.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.agentos.core.common.AgentLogger
import dev.agentos.core.common.DispatcherProvider
import dev.agentos.core.common.Result
import dev.agentos.voice.domain.SpeechResult
import dev.agentos.voice.domain.VoiceInputManager
import dev.agentos.voice.domain.VoiceInputState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android SpeechRecognizer implementation of VoiceInputManager.
 */
@Singleton
class AndroidVoiceInputManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider
) : VoiceInputManager {

    private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    override val state: StateFlow<VoiceInputState> = _state.asStateFlow()

    private val _recognitionResults = MutableSharedFlow<SpeechResult>()
    override val recognitionResults: Flow<SpeechResult> = _recognitionResults.asSharedFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    override suspend fun startListening(): Result<Unit> = withContext(dispatchers.main) {
        if (!isAvailable()) {
            _state.value = VoiceInputState.Error("Speech recognition not available")
            return@withContext Result.error("Speech recognition not available")
        }

        try {
            // Create recognizer on main thread
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }

            val intent = createRecognizerIntent()
            speechRecognizer?.startListening(intent)
            _state.value = VoiceInputState.Listening
            
            AgentLogger.i(AgentLogger.TAG_VOICE, "Started listening")
            Result.success(Unit)
        } catch (e: Exception) {
            AgentLogger.e(AgentLogger.TAG_VOICE, "Failed to start listening", e)
            _state.value = VoiceInputState.Error(e.message ?: "Unknown error")
            Result.error(e)
        }
    }

    override fun stopListening() {
        speechRecognizer?.stopListening()
        _state.value = VoiceInputState.Processing
        AgentLogger.i(AgentLogger.TAG_VOICE, "Stopped listening")
    }

    override fun cancel() {
        speechRecognizer?.cancel()
        _state.value = VoiceInputState.Idle
        AgentLogger.i(AgentLogger.TAG_VOICE, "Cancelled")
    }

    override fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    private fun createRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _state.value = VoiceInputState.Listening
                AgentLogger.d(AgentLogger.TAG_VOICE, "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                AgentLogger.d(AgentLogger.TAG_VOICE, "Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Can be used for visual feedback
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _state.value = VoiceInputState.Processing
                AgentLogger.d(AgentLogger.TAG_VOICE, "Speech ended")
            }

            override fun onError(error: Int) {
                val errorMessage = getErrorMessage(error)
                AgentLogger.e(AgentLogger.TAG_VOICE, "Recognition error: $errorMessage")
                _state.value = VoiceInputState.Error(errorMessage)
                
                // Emit error result
                _recognitionResults.tryEmit(SpeechResult.Error(error, errorMessage))
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidence = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    val conf = confidence?.firstOrNull() ?: 0.8f
                    
                    AgentLogger.logVoiceInput(text)
                    _recognitionResults.tryEmit(SpeechResult.Final(text, conf))
                }
                
                _state.value = VoiceInputState.Idle
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )
                if (!matches.isNullOrEmpty()) {
                    _recognitionResults.tryEmit(SpeechResult.Partial(matches[0]))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error: $errorCode"
        }
    }
}
