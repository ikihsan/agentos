package dev.agentos.llm.domain

import dev.agentos.core.common.Result
import dev.agentos.core.domain.model.Task

/**
 * Interface for LLM client implementations.
 * 
 * Supports different LLM providers (OpenAI, Anthropic, etc.)
 */
interface LlmClient {
    
    /**
     * Sends a prompt to the LLM and receives a raw text response.
     */
    suspend fun complete(prompt: String): Result<String>
    
    /**
     * Sends a conversation history and receives a response.
     */
    suspend fun chat(messages: List<ChatMessage>): Result<String>
    
    /**
     * Parses user input into a structured Task.
     * 
     * This is the primary entry point for intent understanding.
     */
    suspend fun parseIntent(userInput: String, context: IntentContext = IntentContext()): Result<Task>
    
    /**
     * Generates a follow-up question for a missing slot.
     */
    suspend fun generateSlotQuestion(task: Task, slotName: String): Result<String>
    
    /**
     * Generates a natural language confirmation message.
     */
    suspend fun generateConfirmation(task: Task): Result<String>
    
    /**
     * Generates a response message for task completion.
     */
    suspend fun generateCompletionMessage(task: Task): Result<String>
}

/**
 * A message in a chat conversation.
 */
data class ChatMessage(
    val role: MessageRole,
    val content: String
)

enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT
}

/**
 * Context for intent parsing.
 */
data class IntentContext(
    val conversationHistory: List<ChatMessage> = emptyList(),
    val userPreferences: Map<String, Any> = emptyMap(),
    val availableCapabilities: List<String> = emptyList(),
    val currentTime: Long = System.currentTimeMillis()
)
