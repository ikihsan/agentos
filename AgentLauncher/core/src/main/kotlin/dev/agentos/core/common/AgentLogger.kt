package dev.agentos.core.common

import timber.log.Timber

/**
 * Logging utilities for Agent OS.
 * Wraps Timber with domain-specific tags.
 */
object AgentLogger {
    
    private const val TAG_PREFIX = "AgentOS"
    
    // Module-specific tags
    const val TAG_VOICE = "$TAG_PREFIX:Voice"
    const val TAG_LLM = "$TAG_PREFIX:LLM"
    const val TAG_TASK = "$TAG_PREFIX:Task"
    const val TAG_UI = "$TAG_PREFIX:UI"
    const val TAG_AUTOMATION = "$TAG_PREFIX:Automation"
    const val TAG_MEMORY = "$TAG_PREFIX:Memory"
    const val TAG_CORE = "$TAG_PREFIX:Core"
    
    fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }
    
    fun i(tag: String, message: String) {
        Timber.tag(tag).i(message)
    }
    
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).w(throwable, message)
        } else {
            Timber.tag(tag).w(message)
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).e(throwable, message)
        } else {
            Timber.tag(tag).e(message)
        }
    }
    
    fun v(tag: String, message: String) {
        Timber.tag(tag).v(message)
    }
    
    /**
     * Logs task state transitions.
     */
    fun logTaskTransition(taskId: String, from: String, to: String) {
        i(TAG_TASK, "Task [$taskId] transition: $from -> $to")
    }
    
    /**
     * Logs LLM requests.
     */
    fun logLlmRequest(prompt: String) {
        d(TAG_LLM, "LLM Request: ${prompt.take(100)}...")
    }
    
    /**
     * Logs LLM responses.
     */
    fun logLlmResponse(response: String) {
        d(TAG_LLM, "LLM Response: ${response.take(200)}...")
    }
    
    /**
     * Logs voice input.
     */
    fun logVoiceInput(text: String) {
        i(TAG_VOICE, "Voice input: $text")
    }
    
    /**
     * Logs automation actions.
     */
    fun logAutomationAction(action: String, target: String) {
        i(TAG_AUTOMATION, "Action: $action on $target")
    }
}

/**
 * Extension function to log and rethrow exceptions.
 */
inline fun <T> T.logException(tag: String, message: String, block: T.() -> Unit): T {
    try {
        block()
    } catch (e: Exception) {
        AgentLogger.e(tag, message, e)
        throw e
    }
    return this
}
