package dev.agentos.taskengine.domain

import dev.agentos.core.common.AgentLogger
import dev.agentos.core.common.DispatcherProvider
import dev.agentos.core.common.Result
import dev.agentos.core.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core Task Manager - The brain scheduler of Agent OS.
 * 
 * Responsibilities:
 * - Create and manage task lifecycle
 * - Detect missing parameters and trigger UI requests
 * - Coordinate task execution
 * - Maintain task history
 */
@Singleton
class TaskManager @Inject constructor(
    private val taskRepository: TaskRepository,
    private val dispatchers: DispatcherProvider
) {
    private val _currentTask = MutableStateFlow<Task?>(null)
    
    /**
     * The currently active task being processed.
     */
    val currentTask: StateFlow<Task?> = _currentTask.asStateFlow()
    
    /**
     * Flow of all active tasks.
     */
    val activeTasks: Flow<List<Task>> = taskRepository.observeActiveTasks()
    
    /**
     * Events emitted by the task manager.
     */
    private val _events = MutableSharedFlow<TaskEvent>()
    val events: SharedFlow<TaskEvent> = _events.asSharedFlow()

    /**
     * Creates a new task from an intent.
     */
    suspend fun createTask(
        intent: Intent,
        slots: Map<String, Slot> = emptyMap(),
        context: TaskContext = TaskContext()
    ): Result<Task> = withContext(dispatchers.io) {
        Result.runCatching {
            val task = Task(
                intent = intent,
                slots = slots,
                context = context,
                status = TaskStatus.PENDING
            )
            
            AgentLogger.i(
                AgentLogger.TAG_TASK,
                "Creating task: ${task.id} with intent ${intent.fullName}"
            )
            
            // Save the task
            taskRepository.save(task)
            
            // Advance to next logical state
            val advancedTask = TaskStateMachine.advance(task)
            if (advancedTask.status != task.status) {
                taskRepository.save(advancedTask)
                AgentLogger.logTaskTransition(task.id, task.status.name, advancedTask.status.name)
            }
            
            // Set as current task
            _currentTask.value = advancedTask
            
            // Emit creation event
            _events.emit(TaskEvent.Created(advancedTask))
            
            // Check if we need input
            if (advancedTask.status == TaskStatus.NEEDS_INPUT) {
                _events.emit(TaskEvent.NeedsInput(advancedTask, advancedTask.missingSlots))
            }
            
            advancedTask
        }
    }

    /**
     * Updates a slot value in a task.
     */
    suspend fun updateSlot(
        taskId: String,
        slotName: String,
        value: Any?
    ): Result<Task> = withContext(dispatchers.io) {
        Result.runCatching {
            val task = taskRepository.getById(taskId)
                ?: throw IllegalArgumentException("Task not found: $taskId")
            
            AgentLogger.d(
                AgentLogger.TAG_TASK,
                "Updating slot '$slotName' in task ${task.id}"
            )
            
            // Update the slot
            var updatedTask = task.withSlotValue(slotName, value)
            
            // Advance state if appropriate
            updatedTask = TaskStateMachine.advance(updatedTask)
            
            if (updatedTask.status != task.status) {
                AgentLogger.logTaskTransition(taskId, task.status.name, updatedTask.status.name)
            }
            
            taskRepository.save(updatedTask)
            
            if (_currentTask.value?.id == taskId) {
                _currentTask.value = updatedTask
            }
            
            // Emit appropriate event
            when (updatedTask.status) {
                TaskStatus.READY -> _events.emit(TaskEvent.Ready(updatedTask))
                TaskStatus.NEEDS_INPUT -> _events.emit(
                    TaskEvent.NeedsInput(updatedTask, updatedTask.missingSlots)
                )
                else -> _events.emit(TaskEvent.Updated(updatedTask))
            }
            
            updatedTask
        }
    }

    /**
     * Marks a task as ready for execution.
     */
    suspend fun markReady(taskId: String): Result<Task> = withContext(dispatchers.io) {
        Result.runCatching {
            val task = taskRepository.getById(taskId)
                ?: throw IllegalArgumentException("Task not found: $taskId")
            
            if (!task.isReady) {
                throw IllegalStateException(
                    "Cannot mark task ready - missing slots: ${task.missingSlots}"
                )
            }
            
            val readyTask = TaskStateMachine.transition(task, TaskStatus.READY)
            taskRepository.save(readyTask)
            
            AgentLogger.logTaskTransition(taskId, task.status.name, readyTask.status.name)
            
            if (_currentTask.value?.id == taskId) {
                _currentTask.value = readyTask
            }
            
            _events.emit(TaskEvent.Ready(readyTask))
            
            readyTask
        }
    }

    /**
     * Begins execution of a task.
     */
    suspend fun beginExecution(taskId: String): Result<Task> = withContext(dispatchers.io) {
        Result.runCatching {
            val task = taskRepository.getById(taskId)
                ?: throw IllegalArgumentException("Task not found: $taskId")
            
            val executingTask = TaskStateMachine.transition(task, TaskStatus.EXECUTING)
            taskRepository.save(executingTask)
            
            AgentLogger.logTaskTransition(taskId, task.status.name, executingTask.status.name)
            
            if (_currentTask.value?.id == taskId) {
                _currentTask.value = executingTask
            }
            
            _events.emit(TaskEvent.Executing(executingTask))
            
            executingTask
        }
    }

    /**
     * Completes a task with a result.
     */
    suspend fun complete(taskId: String, result: TaskResult): Result<Task> = 
        withContext(dispatchers.io) {
            Result.runCatching {
                val task = taskRepository.getById(taskId)
                    ?: throw IllegalArgumentException("Task not found: $taskId")
                
                val completedTask = task.withResult(result)
                taskRepository.save(completedTask)
                
                AgentLogger.logTaskTransition(taskId, task.status.name, completedTask.status.name)
                AgentLogger.i(
                    AgentLogger.TAG_TASK,
                    "Task $taskId completed: success=${result.success}"
                )
                
                if (_currentTask.value?.id == taskId) {
                    _currentTask.value = null
                }
                
                _events.emit(TaskEvent.Completed(completedTask))
                
                completedTask
            }
        }

    /**
     * Fails a task with an error.
     */
    suspend fun fail(taskId: String, error: TaskError): Result<Task> = 
        withContext(dispatchers.io) {
            Result.runCatching {
                val task = taskRepository.getById(taskId)
                    ?: throw IllegalArgumentException("Task not found: $taskId")
                
                val failedTask = task.withResult(
                    TaskResult(success = false, error = error)
                )
                taskRepository.save(failedTask)
                
                AgentLogger.logTaskTransition(taskId, task.status.name, failedTask.status.name)
                AgentLogger.e(AgentLogger.TAG_TASK, "Task $taskId failed: ${error.message}")
                
                if (_currentTask.value?.id == taskId) {
                    _currentTask.value = null
                }
                
                _events.emit(TaskEvent.Failed(failedTask, error))
                
                failedTask
            }
        }

    /**
     * Cancels a task.
     */
    suspend fun cancel(taskId: String): Result<Task> = withContext(dispatchers.io) {
        Result.runCatching {
            val task = taskRepository.getById(taskId)
                ?: throw IllegalArgumentException("Task not found: $taskId")
            
            if (!TaskStateMachine.canCancel(task)) {
                throw IllegalStateException("Cannot cancel task in state: ${task.status}")
            }
            
            val cancelledTask = TaskStateMachine.transition(task, TaskStatus.CANCELLED)
            taskRepository.save(cancelledTask)
            
            AgentLogger.logTaskTransition(taskId, task.status.name, cancelledTask.status.name)
            
            if (_currentTask.value?.id == taskId) {
                _currentTask.value = null
            }
            
            _events.emit(TaskEvent.Cancelled(cancelledTask))
            
            cancelledTask
        }
    }

    /**
     * Gets a task by ID.
     */
    suspend fun getTask(taskId: String): Task? = withContext(dispatchers.io) {
        taskRepository.getById(taskId)
    }

    /**
     * Gets task history.
     */
    suspend fun getHistory(limit: Int = 50): List<Task> = withContext(dispatchers.io) {
        taskRepository.getHistory(limit)
    }

    /**
     * Sets the current active task.
     */
    fun setCurrentTask(task: Task?) {
        _currentTask.value = task
    }

    /**
     * Observes a specific task.
     */
    fun observeTask(taskId: String): Flow<Task?> = taskRepository.observeTask(taskId)
}

/**
 * Events emitted by the TaskManager.
 */
sealed class TaskEvent {
    data class Created(val task: Task) : TaskEvent()
    data class Updated(val task: Task) : TaskEvent()
    data class NeedsInput(val task: Task, val missingSlots: List<String>) : TaskEvent()
    data class Ready(val task: Task) : TaskEvent()
    data class Executing(val task: Task) : TaskEvent()
    data class Completed(val task: Task) : TaskEvent()
    data class Failed(val task: Task, val error: TaskError) : TaskEvent()
    data class Cancelled(val task: Task) : TaskEvent()
}
