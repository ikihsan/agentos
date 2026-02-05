package dev.agentos.automation.domain

import dev.agentos.core.common.Result

/**
 * Executes actions on behalf of the user.
 * 
 * This is the core interface for app automation in Agent OS.
 */
interface ActionExecutor {
    
    /**
     * Executes an action.
     * 
     * @param action The action to execute
     * @return Result indicating success or failure
     */
    suspend fun execute(action: AutomationAction): Result<ActionResult>
    
    /**
     * Checks if the action can be executed.
     */
    suspend fun canExecute(action: AutomationAction): Boolean
    
    /**
     * Gets the current state of the foreground app.
     */
    suspend fun getCurrentAppState(): AppState?
}

/**
 * Represents an action to be executed.
 */
sealed class AutomationAction {
    /**
     * Launch an app.
     */
    data class LaunchApp(
        val packageName: String,
        val intentAction: String? = null,
        val extras: Map<String, Any>? = null
    ) : AutomationAction()
    
    /**
     * Click on an element.
     */
    data class Click(
        val selector: UiSelector
    ) : AutomationAction()
    
    /**
     * Input text into a field.
     */
    data class InputText(
        val selector: UiSelector,
        val text: String,
        val clearFirst: Boolean = true
    ) : AutomationAction()
    
    /**
     * Scroll in a direction.
     */
    data class Scroll(
        val direction: ScrollDirection,
        val selector: UiSelector? = null
    ) : AutomationAction()
    
    /**
     * Go back (press back button).
     */
    data object Back : AutomationAction()
    
    /**
     * Go home (press home button).
     */
    data object Home : AutomationAction()
    
    /**
     * Open recent apps.
     */
    data object RecentApps : AutomationAction()
    
    /**
     * Open notifications.
     */
    data object OpenNotifications : AutomationAction()
    
    /**
     * Open quick settings.
     */
    data object OpenQuickSettings : AutomationAction()
    
    /**
     * Wait for a condition.
     */
    data class WaitFor(
        val condition: WaitCondition,
        val timeoutMs: Long = 5000
    ) : AutomationAction()
    
    /**
     * Take a screenshot.
     */
    data object Screenshot : AutomationAction()
}

/**
 * Selector for finding UI elements.
 */
data class UiSelector(
    val text: String? = null,
    val textContains: String? = null,
    val contentDescription: String? = null,
    val resourceId: String? = null,
    val className: String? = null,
    val packageName: String? = null,
    val index: Int? = null,
    val clickable: Boolean? = null,
    val scrollable: Boolean? = null
)

/**
 * Scroll direction.
 */
enum class ScrollDirection {
    UP, DOWN, LEFT, RIGHT
}

/**
 * Condition to wait for.
 */
sealed class WaitCondition {
    data class ElementPresent(val selector: UiSelector) : WaitCondition()
    data class ElementGone(val selector: UiSelector) : WaitCondition()
    data class PackageInForeground(val packageName: String) : WaitCondition()
    data class Idle(val timeoutMs: Long = 1000) : WaitCondition()
}

/**
 * Result of an action execution.
 */
sealed class ActionResult {
    data object Success : ActionResult()
    data class Found(val element: UiElementInfo) : ActionResult()
    data class Screenshot(val path: String) : ActionResult()
    data class Error(val message: String) : ActionResult()
}

/**
 * Information about a UI element.
 */
data class UiElementInfo(
    val text: String?,
    val contentDescription: String?,
    val resourceId: String?,
    val className: String?,
    val bounds: Bounds?,
    val isClickable: Boolean,
    val isScrollable: Boolean,
    val isEditable: Boolean
)

/**
 * Bounds of a UI element.
 */
data class Bounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

/**
 * State of the current foreground app.
 */
data class AppState(
    val packageName: String,
    val activityName: String?,
    val windowTitle: String?,
    val elements: List<UiElementInfo>
)
