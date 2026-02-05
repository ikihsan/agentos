package dev.agentos.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents the core Task entity in Agent OS.
 * 
 * A Task is the fundamental unit of user interaction with the platform.
 * It encapsulates user intent, required parameters, execution state,
 * and results.
 * 
 * @property id Unique identifier for this task
 * @property intent The classified intent (domain.action)
 * @property status Current lifecycle state
 * @property slots Parameters required for task execution
 * @property context Contextual information about task creation
 * @property result Execution results when completed
 * @property createdAt Timestamp of task creation
 * @property updatedAt Timestamp of last state change
 */
@Serializable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val intent: Intent,
    val status: TaskStatus = TaskStatus.PENDING,
    val slots: Map<String, Slot> = emptyMap(),
    val context: TaskContext = TaskContext(),
    val result: TaskResult? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Returns list of slot names that are required but not yet resolved.
     */
    val missingSlots: List<String>
        get() = slots.filter { it.value.required && !it.value.resolved }.keys.toList()

    /**
     * Returns true if all required slots are resolved.
     */
    val isReady: Boolean
        get() = missingSlots.isEmpty()

    /**
     * Returns true if the task is in a terminal state.
     */
    val isTerminal: Boolean
        get() = status in listOf(TaskStatus.COMPLETED, TaskStatus.FAILED, TaskStatus.CANCELLED)

    /**
     * Creates a copy with updated status and timestamp.
     */
    fun withStatus(newStatus: TaskStatus): Task = copy(
        status = newStatus,
        updatedAt = System.currentTimeMillis()
    )

    /**
     * Creates a copy with an updated slot value.
     */
    fun withSlotValue(slotName: String, value: Any?): Task {
        val slot = slots[slotName] ?: return this
        val updatedSlot = slot.copy(
            value = value,
            resolved = value != null
        )
        return copy(
            slots = slots + (slotName to updatedSlot),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Creates a copy with execution result.
     */
    fun withResult(taskResult: TaskResult): Task = copy(
        result = taskResult,
        status = if (taskResult.success) TaskStatus.COMPLETED else TaskStatus.FAILED,
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Represents a classified user intent.
 * 
 * Intents follow the pattern: domain.action
 * Examples: messaging.send_text, transport.book_ride
 */
@Serializable
data class Intent(
    val domain: String,
    val action: String,
    val confidence: Float = 1.0f
) {
    /**
     * Full intent identifier in domain.action format.
     */
    val fullName: String
        get() = "$domain.$action"

    companion object {
        fun parse(fullName: String): Intent? {
            val parts = fullName.split(".")
            return if (parts.size >= 2) {
                Intent(
                    domain = parts[0],
                    action = parts.drop(1).joinToString(".")
                )
            } else null
        }
    }
}

/**
 * Task lifecycle states.
 */
@Serializable
enum class TaskStatus {
    @SerialName("pending")
    PENDING,           // Task created, not yet processed

    @SerialName("needs_input")
    NEEDS_INPUT,       // Waiting for user input to fill slots

    @SerialName("ready")
    READY,             // All slots filled, ready for execution

    @SerialName("executing")
    EXECUTING,         // Currently being executed

    @SerialName("completed")
    COMPLETED,         // Successfully completed

    @SerialName("failed")
    FAILED,            // Execution failed

    @SerialName("cancelled")
    CANCELLED          // Cancelled by user or system
}

/**
 * Represents a parameter slot in a task.
 */
@Serializable
data class Slot(
    val name: String,
    val type: SlotType,
    val required: Boolean = true,
    val value: @Serializable(with = AnySerializer::class) Any? = null,
    val resolved: Boolean = false,
    val description: String? = null,
    val constraints: SlotConstraints? = null
)

/**
 * Supported slot value types.
 */
@Serializable
enum class SlotType {
    @SerialName("string")
    STRING,

    @SerialName("number")
    NUMBER,

    @SerialName("boolean")
    BOOLEAN,

    @SerialName("date")
    DATE,

    @SerialName("datetime")
    DATETIME,

    @SerialName("contact")
    CONTACT,

    @SerialName("contacts")
    CONTACTS,

    @SerialName("media")
    MEDIA,

    @SerialName("location")
    LOCATION,

    @SerialName("address")
    ADDRESS,

    @SerialName("currency")
    CURRENCY,

    @SerialName("enum")
    ENUM,

    @SerialName("object")
    OBJECT,

    @SerialName("array")
    ARRAY
}

/**
 * Constraints for slot values.
 */
@Serializable
data class SlotConstraints(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val min: Double? = null,
    val max: Double? = null,
    val pattern: String? = null,
    val enumValues: List<String>? = null
)

/**
 * Contextual information about task creation.
 */
@Serializable
data class TaskContext(
    val source: InputSource = InputSource.TEXT,
    val rawInput: String? = null,
    val sessionId: String? = null,
    val parentTaskId: String? = null,
    val conversationHistory: List<ConversationTurn> = emptyList()
)

/**
 * Input source for the task.
 */
@Serializable
enum class InputSource {
    @SerialName("voice")
    VOICE,

    @SerialName("text")
    TEXT,

    @SerialName("gesture")
    GESTURE,

    @SerialName("automation")
    AUTOMATION
}

/**
 * A single turn in the conversation history.
 */
@Serializable
data class ConversationTurn(
    val role: ConversationRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class ConversationRole {
    @SerialName("user")
    USER,

    @SerialName("assistant")
    ASSISTANT,

    @SerialName("system")
    SYSTEM
}

/**
 * Result of task execution.
 */
@Serializable
data class TaskResult(
    val success: Boolean,
    val data: Map<String, @Serializable(with = AnySerializer::class) Any?> = emptyMap(),
    val error: TaskError? = null,
    val executedAt: Long = System.currentTimeMillis()
)

/**
 * Error information for failed tasks.
 */
@Serializable
data class TaskError(
    val code: String,
    val message: String,
    val recoverable: Boolean = false,
    val details: Map<String, String> = emptyMap()
)
