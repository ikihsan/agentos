package dev.agentos.automation.data

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import dev.agentos.automation.domain.*
import dev.agentos.automation.service.AgentAccessibilityService
import dev.agentos.core.common.AgentLogger
import dev.agentos.core.common.DispatcherProvider
import dev.agentos.core.common.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ActionExecutor using AccessibilityService.
 */
@Singleton
class AccessibilityActionExecutor @Inject constructor(
    private val appLauncher: AppLauncher,
    private val dispatchers: DispatcherProvider
) : ActionExecutor {

    private val service: AgentAccessibilityService?
        get() = AgentAccessibilityService.instance

    override suspend fun execute(action: AutomationAction): Result<ActionResult> {
        return withContext(dispatchers.main) {
            when (action) {
                is AutomationAction.LaunchApp -> executeLaunchApp(action)
                is AutomationAction.Click -> executeClick(action)
                is AutomationAction.InputText -> executeInputText(action)
                is AutomationAction.Scroll -> executeScroll(action)
                is AutomationAction.Back -> executeGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                is AutomationAction.Home -> executeGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                is AutomationAction.RecentApps -> executeGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
                is AutomationAction.OpenNotifications -> executeGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
                is AutomationAction.OpenQuickSettings -> executeGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
                is AutomationAction.WaitFor -> executeWait(action)
                is AutomationAction.Screenshot -> executeScreenshot()
            }
        }
    }

    override suspend fun canExecute(action: AutomationAction): Boolean {
        return when (action) {
            is AutomationAction.LaunchApp -> appLauncher.isAppInstalled(action.packageName)
            else -> service != null
        }
    }

    override suspend fun getCurrentAppState(): AppState? {
        return service?.getAppState()
    }

    private fun executeLaunchApp(action: AutomationAction.LaunchApp): Result<ActionResult> {
        return when (val result = appLauncher.launchApp(action.packageName)) {
            is Result.Success -> Result.success(ActionResult.Success)
            is Result.Error -> Result.error(result.message ?: "Failed to launch app")
            is Result.Loading -> Result.error("Unexpected loading state")
        }
    }

    private fun executeClick(action: AutomationAction.Click): Result<ActionResult> {
        val svc = service ?: return Result.error("Accessibility service not connected")
        
        val node = svc.findNode(action.selector)
            ?: return Result.error("Element not found")
        
        return if (svc.clickNode(node)) {
            AgentLogger.logAutomationAction("Click", action.selector.toString())
            Result.success(ActionResult.Success)
        } else {
            Result.error("Click failed")
        }
    }

    private fun executeInputText(action: AutomationAction.InputText): Result<ActionResult> {
        val svc = service ?: return Result.error("Accessibility service not connected")
        
        val node = svc.findNode(action.selector)
            ?: return Result.error("Element not found")
        
        // Focus on the node first
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        
        return if (svc.setNodeText(node, action.text, action.clearFirst)) {
            AgentLogger.logAutomationAction("InputText", "${action.selector}: ${action.text}")
            Result.success(ActionResult.Success)
        } else {
            Result.error("Failed to input text")
        }
    }

    private fun executeScroll(action: AutomationAction.Scroll): Result<ActionResult> {
        val svc = service ?: return Result.error("Accessibility service not connected")
        
        val node = action.selector?.let { svc.findNode(it) }
        
        return if (svc.scroll(action.direction, node)) {
            Result.success(ActionResult.Success)
        } else {
            Result.error("Scroll failed")
        }
    }

    private fun executeGlobalAction(action: Int): Result<ActionResult> {
        val svc = service ?: return Result.error("Accessibility service not connected")
        
        return if (svc.performGlobalAction(action)) {
            Result.success(ActionResult.Success)
        } else {
            Result.error("Global action failed")
        }
    }

    private suspend fun executeWait(action: AutomationAction.WaitFor): Result<ActionResult> {
        val svc = service ?: return Result.error("Accessibility service not connected")
        
        val result = withTimeoutOrNull(action.timeoutMs) {
            when (val condition = action.condition) {
                is WaitCondition.ElementPresent -> {
                    while (svc.findNode(condition.selector) == null) {
                        delay(100)
                    }
                    true
                }
                is WaitCondition.ElementGone -> {
                    while (svc.findNode(condition.selector) != null) {
                        delay(100)
                    }
                    true
                }
                is WaitCondition.PackageInForeground -> {
                    while (AgentAccessibilityService.currentPackage.value != condition.packageName) {
                        delay(100)
                    }
                    true
                }
                is WaitCondition.Idle -> {
                    delay(condition.timeoutMs)
                    true
                }
            }
        }
        
        return if (result == true) {
            Result.success(ActionResult.Success)
        } else {
            Result.error("Wait timed out")
        }
    }

    private fun executeScreenshot(): Result<ActionResult> {
        val svc = service ?: return Result.error("Accessibility service not connected")
        
        return if (svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)) {
            Result.success(ActionResult.Screenshot("screenshot_taken"))
        } else {
            Result.error("Screenshot failed")
        }
    }
}
