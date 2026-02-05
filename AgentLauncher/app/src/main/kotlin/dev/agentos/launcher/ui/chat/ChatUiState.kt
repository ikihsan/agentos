package dev.agentos.launcher.ui.chat

import dev.agentos.core.model.Task
import dev.agentos.core.model.UiComponent
import dev.agentos.voice.domain.VoiceInputState

/**
 * UI state for the chat screen.
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentTask: Task? = null,
    val dynamicUiComponents: List<UiComponent> = emptyList(),
    val voiceState: VoiceInputState = VoiceInputState.Idle,
    val isProcessing: Boolean = false,
    val partialSpeech: String? = null,
    val error: String? = null
)

/**
 * Represents a chat message.
 */
data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val taskId: String? = null
)
