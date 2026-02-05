package dev.agentos.taskengine.domain

import dev.agentos.core.domain.model.Task
import dev.agentos.core.domain.model.TaskStatus

/**
 * State machine for Task lifecycle management.
 * 
 * Defines valid state transitions and enforces the Task lifecycle contract.
 * 
 * State Diagram:
 * ```
 * PENDING ──────────────────┬──► NEEDS_INPUT ──► READY ──► EXECUTING ──► COMPLETED
 *    │                      │         │            │           │
 *    │                      │         │            │           └──► FAILED
 *    │                      │         │            │
 *    └──────────────────────┴─────────┴────────────┴──────────────► CANCELLED
 * ```
 */
object TaskStateMachine {

    /**
     * Defines valid transitions from each state.
     */
    private val validTransitions: Map<TaskStatus, Set<TaskStatus>> = mapOf(
        TaskStatus.PENDING to setOf(
            TaskStatus.NEEDS_INPUT,
            TaskStatus.READY,
            TaskStatus.CANCELLED
        ),
        TaskStatus.NEEDS_INPUT to setOf(
            TaskStatus.READY,
            TaskStatus.NEEDS_INPUT, // Can cycle back for additional inputs
            TaskStatus.CANCELLED
        ),
        TaskStatus.READY to setOf(
            TaskStatus.EXECUTING,
            TaskStatus.NEEDS_INPUT, // May need to go back if validation fails
            TaskStatus.CANCELLED
        ),
        TaskStatus.EXECUTING to setOf(
            TaskStatus.COMPLETED,
            TaskStatus.FAILED,
            TaskStatus.CANCELLED
        ),
        // Terminal states - no transitions allowed
        TaskStatus.COMPLETED to emptySet(),
        TaskStatus.FAILED to emptySet(),
        TaskStatus.CANCELLED to emptySet()
    )

    /**
     * Checks if a transition from [from] to [to] is valid.
     */
    fun isValidTransition(from: TaskStatus, to: TaskStatus): Boolean {
        return validTransitions[from]?.contains(to) ?: false
    }

    /**
     * Attempts to transition a task to a new state.
     * 
     * @param task The task to transition
     * @param newStatus The target status
     * @return Result containing the transitioned task or an error
     * @throws IllegalStateException if the transition is invalid
     */
    fun transition(task: Task, newStatus: TaskStatus): Task {
        if (!isValidTransition(task.status, newStatus)) {
            throw IllegalStateException(
                "Invalid task state transition: ${task.status} -> $newStatus for task ${task.id}"
            )
        }
        return task.withStatus(newStatus)
    }

    /**
     * Attempts to transition and returns null if invalid.
     */
    fun tryTransition(task: Task, newStatus: TaskStatus): Task? {
        return if (isValidTransition(task.status, newStatus)) {
            task.withStatus(newStatus)
        } else {
            null
        }
    }

    /**
     * Computes the next state based on task state.
     * 
     * This is the automatic state resolution logic:
     * - If task has missing slots → NEEDS_INPUT
     * - If task is ready (all slots filled) → READY
     * - Otherwise maintains current state
     */
    fun computeNextState(task: Task): TaskStatus {
        return when (task.status) {
            TaskStatus.PENDING -> {
                if (task.missingSlots.isEmpty()) TaskStatus.READY
                else TaskStatus.NEEDS_INPUT
            }
            TaskStatus.NEEDS_INPUT -> {
                if (task.missingSlots.isEmpty()) TaskStatus.READY
                else TaskStatus.NEEDS_INPUT
            }
            else -> task.status
        }
    }

    /**
     * Automatically advances task to its next logical state.
     */
    fun advance(task: Task): Task {
        val nextState = computeNextState(task)
        return if (nextState != task.status && isValidTransition(task.status, nextState)) {
            task.withStatus(nextState)
        } else {
            task
        }
    }

    /**
     * Returns all valid next states for the given task.
     */
    fun getValidNextStates(task: Task): Set<TaskStatus> {
        return validTransitions[task.status] ?: emptySet()
    }

    /**
     * Checks if a task is in a terminal state.
     */
    fun isTerminal(status: TaskStatus): Boolean {
        return validTransitions[status]?.isEmpty() ?: true
    }

    /**
     * Checks if a task can be cancelled from its current state.
     */
    fun canCancel(task: Task): Boolean {
        return isValidTransition(task.status, TaskStatus.CANCELLED)
    }
}
