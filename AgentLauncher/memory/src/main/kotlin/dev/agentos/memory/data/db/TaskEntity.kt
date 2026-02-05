package dev.agentos.memory.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room entity for persisted tasks.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    
    val utterance: String,
    
    @ColumnInfo(name = "intent_domain")
    val intentDomain: String,
    
    @ColumnInfo(name = "intent_action")
    val intentAction: String,
    
    @ColumnInfo(name = "intent_confidence")
    val intentConfidence: Float,
    
    val status: String,
    
    @ColumnInfo(name = "slots_json")
    val slotsJson: String, // JSON serialized slots
    
    @ColumnInfo(name = "result_json")
    val resultJson: String?, // JSON serialized result
    
    @ColumnInfo(name = "context_json")
    val contextJson: String?, // JSON serialized context
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

/**
 * DAO for Task operations.
 */
@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeTask(id: String): Flow<TaskEntity?>
    
    @Query("SELECT * FROM tasks WHERE status IN (:statuses) ORDER BY created_at DESC")
    fun getTasksByStatus(vararg statuses: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks ORDER BY created_at DESC LIMIT :limit")
    fun getRecentTasks(limit: Int): Flow<List<TaskEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)
    
    @Update
    suspend fun update(task: TaskEntity)
    
    @Delete
    suspend fun delete(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM tasks WHERE status = :status AND created_at < :beforeTimestamp")
    suspend fun deleteOldTasksByStatus(status: String, beforeTimestamp: Long): Int
    
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int
}
