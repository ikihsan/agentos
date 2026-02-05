package dev.agentos.memory.data.repository

import dev.agentos.core.common.DispatcherProvider
import dev.agentos.memory.data.db.MessageDao
import dev.agentos.memory.data.db.MessageEntity
import dev.agentos.memory.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of MessageRepository.
 */
@Singleton
class RoomMessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val json: Json,
    private val dispatchers: DispatcherProvider
) : MessageRepository {

    override fun observeAllMessages(): Flow<List<Message>> {
        return messageDao.getAllMessages().map { entities ->
            entities.map { it.toMessage() }
        }
    }

    override fun observeMessagesForTask(taskId: String): Flow<List<Message>> {
        return messageDao.getMessagesForTask(taskId).map { entities ->
            entities.map { it.toMessage() }
        }
    }

    override fun getRecentMessages(limit: Int): Flow<List<Message>> {
        return messageDao.getRecentMessages(limit).map { entities ->
            entities.map { it.toMessage() }
        }
    }

    override suspend fun saveMessage(message: Message) {
        withContext(dispatchers.io) {
            messageDao.insert(message.toEntity())
        }
    }

    override suspend fun deleteMessagesForTask(taskId: String) {
        withContext(dispatchers.io) {
            messageDao.deleteMessagesForTask(taskId)
        }
    }

    override suspend fun cleanupOldMessages(olderThanMs: Long): Int {
        return withContext(dispatchers.io) {
            val beforeTimestamp = System.currentTimeMillis() - olderThanMs
            messageDao.deleteOldMessages(beforeTimestamp)
        }
    }

    private fun MessageEntity.toMessage(): Message {
        val metadata = metadataJson?.let {
            try {
                json.decodeFromString<Map<String, Any?>>(it)
            } catch (e: Exception) {
                null
            }
        }
        
        return Message(
            id = id,
            taskId = taskId,
            role = MessageRole.valueOf(role.uppercase()),
            content = content,
            contentType = ContentType.valueOf(contentType.uppercase()),
            metadata = metadata,
            createdAt = createdAt
        )
    }

    private fun Message.toEntity(): MessageEntity {
        return MessageEntity(
            id = id,
            taskId = taskId,
            role = role.name.lowercase(),
            content = content,
            contentType = contentType.name.lowercase(),
            metadataJson = metadata?.let { json.encodeToString(it) },
            createdAt = createdAt
        )
    }
}
