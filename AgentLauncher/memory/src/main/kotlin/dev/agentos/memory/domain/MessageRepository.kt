package dev.agentos.memory.domain

import kotlinx.coroutines.flow.Flow

/**
 * Represents a conversation message.
 */
data class Message(
    val id: String,
    val taskId: String?,
    val role: MessageRole,
    val content: String,
    val contentType: ContentType,
    val metadata: Map<String, Any?>? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class ContentType {
    TEXT,
    VOICE,
    UI_RESPONSE
}

/**
 * Repository for conversation messages.
 */
interface MessageRepository {
    
    /**
     * Observes all messages.
     */
    fun observeAllMessages(): Flow<List<Message>>
    
    /**
     * Observes messages for a specific task.
     */
    fun observeMessagesForTask(taskId: String): Flow<List<Message>>
    
    /**
     * Gets recent messages for context.
     */
    fun getRecentMessages(limit: Int = 20): Flow<List<Message>>
    
    /**
     * Saves a message.
     */
    suspend fun saveMessage(message: Message)
    
    /**
     * Deletes messages for a task.
     */
    suspend fun deleteMessagesForTask(taskId: String)
    
    /**
     * Cleans up old messages.
     */
    suspend fun cleanupOldMessages(olderThanMs: Long): Int
}
