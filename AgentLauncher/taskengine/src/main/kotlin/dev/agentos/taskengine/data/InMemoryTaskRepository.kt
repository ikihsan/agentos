package dev.agentos.taskengine.data

import dev.agentos.core.domain.model.Task
import dev.agentos.taskengine.domain.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory implementation of TaskRepository.
 * 
 * Used for initial development and testing.
 * Will be replaced with Room-based persistence in production.
 */
@Singleton
class InMemoryTaskRepository @Inject constructor() : TaskRepository {
    
    private val tasks = MutableStateFlow<Map<String, Task>>(emptyMap())
    private val mutex = Mutex()
    
    override suspend fun save(task: Task) {
        mutex.withLock {
            tasks.value = tasks.value + (task.id to task)
        }
    }
    
    override suspend fun getById(id: String): Task? {
        return tasks.value[id]
    }
    
    override suspend fun getActiveTasks(): List<Task> {
        return tasks.value.values
            .filter { !it.isTerminal }
            .sortedByDescending { it.updatedAt }
    }
    
    override suspend fun getHistory(limit: Int): List<Task> {
        return tasks.value.values
            .filter { it.isTerminal }
            .sortedByDescending { it.updatedAt }
            .take(limit)
    }
    
    override suspend fun delete(id: String) {
        mutex.withLock {
            tasks.value = tasks.value - id
        }
    }
    
    override suspend fun clearOldTasks(beforeTimestamp: Long) {
        mutex.withLock {
            tasks.value = tasks.value.filterValues { 
                !it.isTerminal || it.updatedAt >= beforeTimestamp 
            }
        }
    }
    
    override fun observeTask(id: String): Flow<Task?> {
        return tasks.map { it[id] }
    }
    
    override fun observeActiveTasks(): Flow<List<Task>> {
        return tasks.map { taskMap ->
            taskMap.values
                .filter { !it.isTerminal }
                .sortedByDescending { it.updatedAt }
        }
    }
}
