package dev.agentos.memory.data.repository

import dev.agentos.core.common.DispatcherProvider
import dev.agentos.core.model.*
import dev.agentos.memory.data.db.TaskDao
import dev.agentos.memory.data.db.TaskEntity
import dev.agentos.memory.domain.PersistentTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of PersistentTaskRepository.
 */
@Singleton
class RoomTaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val json: Json,
    private val dispatchers: DispatcherProvider
) : PersistentTaskRepository {

    override fun observeAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.mapNotNull { it.toTask() }
        }
    }

    override fun observeTask(taskId: String): Flow<Task?> {
        return taskDao.observeTask(taskId).map { it?.toTask() }
    }

    override fun getRecentTasks(limit: Int): Flow<List<Task>> {
        return taskDao.getRecentTasks(limit).map { entities ->
            entities.mapNotNull { it.toTask() }
        }
    }

    override fun getTasksByStatus(vararg statuses: String): Flow<List<Task>> {
        return taskDao.getTasksByStatus(*statuses).map { entities ->
            entities.mapNotNull { it.toTask() }
        }
    }

    override suspend fun getTask(taskId: String): Task? {
        return withContext(dispatchers.io) {
            taskDao.getTaskById(taskId)?.toTask()
        }
    }

    override suspend fun saveTask(task: Task) {
        withContext(dispatchers.io) {
            taskDao.insert(task.toEntity())
        }
    }

    override suspend fun deleteTask(taskId: String) {
        withContext(dispatchers.io) {
            taskDao.deleteById(taskId)
        }
    }

    override suspend fun cleanupOldTasks(olderThanMs: Long): Int {
        return withContext(dispatchers.io) {
            val beforeTimestamp = System.currentTimeMillis() - olderThanMs
            val completedDeleted = taskDao.deleteOldTasksByStatus(
                TaskStatus.COMPLETED.name, 
                beforeTimestamp
            )
            val cancelledDeleted = taskDao.deleteOldTasksByStatus(
                TaskStatus.CANCELLED.name, 
                beforeTimestamp
            )
            completedDeleted + cancelledDeleted
        }
    }

    private fun TaskEntity.toTask(): Task? {
        return try {
            val slots = json.decodeFromString<List<Slot>>(slotsJson)
            val result = resultJson?.let { json.decodeFromString<TaskResult>(it) }
            val context = contextJson?.let { json.decodeFromString<TaskContext>(it) }
            
            Task(
                id = id,
                utterance = utterance,
                intent = Intent(
                    domain = intentDomain,
                    action = intentAction,
                    confidence = intentConfidence
                ),
                status = TaskStatus.valueOf(status),
                slots = slots,
                result = result,
                context = context,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun Task.toEntity(): TaskEntity {
        return TaskEntity(
            id = id,
            utterance = utterance,
            intentDomain = intent.domain,
            intentAction = intent.action,
            intentConfidence = intent.confidence,
            status = status.name,
            slotsJson = json.encodeToString(slots),
            resultJson = result?.let { json.encodeToString(it) },
            contextJson = context?.let { json.encodeToString(it) },
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
