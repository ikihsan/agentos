package dev.agentos.voice.domain

import dev.agentos.core.common.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * Text-to-speech manager for Agent OS responses.
 */
interface TextToSpeechManager {
    
    /**
     * Current TTS state.
     */
    val state: StateFlow<TtsState>
    
    /**
     * Speaks the given text.
     */
    suspend fun speak(text: String): Result<Unit>
    
    /**
     * Speaks with priority (interrupts current speech).
     */
    suspend fun speakImmediate(text: String): Result<Unit>
    
    /**
     * Stops current speech.
     */
    fun stop()
    
    /**
     * Checks if TTS is available.
     */
    fun isAvailable(): Boolean
    
    /**
     * Sets speech rate (0.5 to 2.0, default 1.0).
     */
    fun setSpeechRate(rate: Float)
    
    /**
     * Sets pitch (0.5 to 2.0, default 1.0).
     */
    fun setPitch(pitch: Float)
}

/**
 * TTS states.
 */
sealed class TtsState {
    data object Idle : TtsState()
    data object Speaking : TtsState()
    data object Initializing : TtsState()
    data class Error(val message: String) : TtsState()
}
