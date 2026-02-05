package dev.agentos.voice.domain

import dev.agentos.core.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages voice input for Agent OS.
 * 
 * Coordinates speech recognition and provides voice input state.
 */
interface VoiceInputManager {
    
    /**
     * Current state of voice input.
     */
    val state: StateFlow<VoiceInputState>
    
    /**
     * Flow of recognized speech results.
     */
    val recognitionResults: Flow<SpeechResult>
    
    /**
     * Starts listening for voice input.
     */
    suspend fun startListening(): Result<Unit>
    
    /**
     * Stops listening for voice input.
     */
    fun stopListening()
    
    /**
     * Cancels current recognition.
     */
    fun cancel()
    
    /**
     * Checks if voice input is available on this device.
     */
    fun isAvailable(): Boolean
}

/**
 * States of voice input.
 */
sealed class VoiceInputState {
    data object Idle : VoiceInputState()
    data object Listening : VoiceInputState()
    data object Processing : VoiceInputState()
    data class Error(val message: String) : VoiceInputState()
}

/**
 * Result from speech recognition.
 */
sealed class SpeechResult {
    data class Partial(val text: String) : SpeechResult()
    data class Final(val text: String, val confidence: Float) : SpeechResult()
    data class Error(val errorCode: Int, val message: String) : SpeechResult()
}
