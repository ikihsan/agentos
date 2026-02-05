package dev.agentos.memory.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room entity for conversation messages.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("task_id")]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "task_id")
    val taskId: String?,
    
    val role: String, // "user", "assistant", "system"
    
    val content: String,
    
    @ColumnInfo(name = "content_type")
    val contentType: String, // "text", "voice", "ui_response"
    
    @ColumnInfo(name = "metadata_json")
    val metadataJson: String?, // JSON for extra data
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

/**
 * DAO for Message operations.
 */
@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages ORDER BY created_at DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE task_id = :taskId ORDER BY created_at ASC")
    fun getMessagesForTask(taskId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages ORDER BY created_at DESC LIMIT :limit")
    fun getRecentMessages(limit: Int): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE task_id IS NULL ORDER BY created_at DESC LIMIT :limit")
    fun getRecentGeneralMessages(limit: Int): Flow<List<MessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)
    
    @Delete
    suspend fun delete(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE task_id = :taskId")
    suspend fun deleteMessagesForTask(taskId: String)
    
    @Query("DELETE FROM messages WHERE created_at < :beforeTimestamp")
    suspend fun deleteOldMessages(beforeTimestamp: Long): Int
}
