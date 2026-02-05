package dev.agentos.taskengine.domain

import dev.agentos.core.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Task persistence.
 */
interface TaskRepository {
    
    /**
     * Saves a task. If a task with the same ID exists, it will be updated.
     */
    suspend fun save(task: Task)
    
    /**
     * Retrieves a task by ID.
     */
    suspend fun getById(id: String): Task?
    
    /**
     * Retrieves all active (non-terminal) tasks.
     */
    suspend fun getActiveTasks(): List<Task>
    
    /**
     * Retrieves task history with optional limit.
     */
    suspend fun getHistory(limit: Int = 50): List<Task>
    
    /**
     * Deletes a task by ID.
     */
    suspend fun delete(id: String)
    
    /**
     * Clears all completed and failed tasks older than the given timestamp.
     */
    suspend fun clearOldTasks(beforeTimestamp: Long)
    
    /**
     * Observes a single task by ID.
     */
    fun observeTask(id: String): Flow<Task?>
    
    /**
     * Observes all active tasks.
     */
    fun observeActiveTasks(): Flow<List<Task>>
}
