package dev.agentos.memory.domain

import dev.agentos.core.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository for persisting tasks.
 */
interface PersistentTaskRepository {
    
    /**
     * Observes all tasks.
     */
    fun observeAllTasks(): Flow<List<Task>>
    
    /**
     * Observes a specific task.
     */
    fun observeTask(taskId: String): Flow<Task?>
    
    /**
     * Gets recent tasks.
     */
    fun getRecentTasks(limit: Int = 50): Flow<List<Task>>
    
    /**
     * Gets tasks by status.
     */
    fun getTasksByStatus(vararg statuses: String): Flow<List<Task>>
    
    /**
     * Gets a task by ID.
     */
    suspend fun getTask(taskId: String): Task?
    
    /**
     * Saves a task.
     */
    suspend fun saveTask(task: Task)
    
    /**
     * Deletes a task.
     */
    suspend fun deleteTask(taskId: String)
    
    /**
     * Cleans up old completed/cancelled tasks.
     */
    suspend fun cleanupOldTasks(olderThanMs: Long): Int
}
