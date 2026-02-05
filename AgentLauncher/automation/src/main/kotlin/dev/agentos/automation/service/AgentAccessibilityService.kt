package dev.agentos.automation.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import dev.agentos.automation.domain.*
import dev.agentos.core.common.AgentLogger
import dev.agentos.core.common.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AccessibilityService for Agent OS automation.
 * 
 * This service enables Agent OS to:
 * - Observe UI state
 * - Perform actions on apps
 * - Automate workflows
 */
class AgentAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _isConnected = MutableStateFlow(false)
    private val _currentPackage = MutableStateFlow<String?>(null)
    private val _currentActivity = MutableStateFlow<String?>(null)

    override fun onServiceConnected() {
        super.onServiceConnected()
        
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }
        
        instance = this
        _isConnected.value = true
        
        AgentLogger.i(AgentLogger.TAG_AUTOMATION, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                _currentPackage.value = event.packageName?.toString()
                _currentActivity.value = event.className?.toString()
                
                AgentLogger.d(
                    AgentLogger.TAG_AUTOMATION, 
                    "Window changed: ${event.packageName}/${event.className}"
                )
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Content changed, could update UI tree if needed
            }
        }
    }

    override fun onInterrupt() {
        AgentLogger.w(AgentLogger.TAG_AUTOMATION, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        instance = null
        _isConnected.value = false
        AgentLogger.i(AgentLogger.TAG_AUTOMATION, "Accessibility service destroyed")
    }

    /**
     * Finds a node matching the selector.
     */
    fun findNode(selector: UiSelector): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        return findNodeRecursive(rootNode, selector)
    }

    /**
     * Finds all nodes matching the selector.
     */
    fun findAllNodes(selector: UiSelector): List<AccessibilityNodeInfo> {
        val rootNode = rootInActiveWindow ?: return emptyList()
        val results = mutableListOf<AccessibilityNodeInfo>()
        findAllNodesRecursive(rootNode, selector, results)
        return results
    }

    private fun findNodeRecursive(
        node: AccessibilityNodeInfo,
        selector: UiSelector
    ): AccessibilityNodeInfo? {
        if (matchesSelector(node, selector)) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeRecursive(child, selector)
            if (found != null) return found
        }
        
        return null
    }

    private fun findAllNodesRecursive(
        node: AccessibilityNodeInfo,
        selector: UiSelector,
        results: MutableList<AccessibilityNodeInfo>
    ) {
        if (matchesSelector(node, selector)) {
            results.add(node)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findAllNodesRecursive(child, selector, results)
        }
    }

    private fun matchesSelector(node: AccessibilityNodeInfo, selector: UiSelector): Boolean {
        selector.text?.let { 
            if (node.text?.toString() != it) return false 
        }
        selector.textContains?.let { 
            if (node.text?.toString()?.contains(it, ignoreCase = true) != true) return false 
        }
        selector.contentDescription?.let { 
            if (node.contentDescription?.toString() != it) return false 
        }
        selector.resourceId?.let { 
            if (node.viewIdResourceName != it) return false 
        }
        selector.className?.let { 
            if (node.className?.toString() != it) return false 
        }
        selector.packageName?.let { 
            if (node.packageName?.toString() != it) return false 
        }
        selector.clickable?.let { 
            if (node.isClickable != it) return false 
        }
        selector.scrollable?.let { 
            if (node.isScrollable != it) return false 
        }
        return true
    }

    /**
     * Clicks on a node.
     */
    fun clickNode(node: AccessibilityNodeInfo): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    /**
     * Sets text on a node.
     */
    fun setNodeText(node: AccessibilityNodeInfo, text: String, clearFirst: Boolean): Boolean {
        if (clearFirst) {
            val clearArgs = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, 
                    ""
                )
            }
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, clearArgs)
        }
        
        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, 
                text
            )
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    /**
     * Scrolls in a direction.
     */
    fun scroll(direction: ScrollDirection, node: AccessibilityNodeInfo? = null): Boolean {
        val targetNode = node ?: rootInActiveWindow ?: return false
        
        val action = when (direction) {
            ScrollDirection.UP, ScrollDirection.LEFT -> 
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            ScrollDirection.DOWN, ScrollDirection.RIGHT -> 
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        }
        
        return targetNode.performAction(action)
    }

    /**
     * Performs global actions.
     */
    fun performGlobalAction(action: Int): Boolean {
        return performGlobalAction(action)
    }

    /**
     * Gets the current app state.
     */
    fun getAppState(): AppState? {
        val rootNode = rootInActiveWindow ?: return null
        
        val elements = mutableListOf<UiElementInfo>()
        collectElements(rootNode, elements, maxDepth = 10, currentDepth = 0)
        
        return AppState(
            packageName = _currentPackage.value ?: rootNode.packageName?.toString() ?: "",
            activityName = _currentActivity.value,
            windowTitle = rootNode.text?.toString(),
            elements = elements
        )
    }

    private fun collectElements(
        node: AccessibilityNodeInfo,
        elements: MutableList<UiElementInfo>,
        maxDepth: Int,
        currentDepth: Int
    ) {
        if (currentDepth > maxDepth) return
        
        // Only collect interesting nodes
        if (node.isClickable || node.isEditable || 
            !node.text.isNullOrEmpty() || !node.contentDescription.isNullOrEmpty()) {
            
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            
            elements.add(
                UiElementInfo(
                    text = node.text?.toString(),
                    contentDescription = node.contentDescription?.toString(),
                    resourceId = node.viewIdResourceName,
                    className = node.className?.toString(),
                    bounds = Bounds(bounds.left, bounds.top, bounds.right, bounds.bottom),
                    isClickable = node.isClickable,
                    isScrollable = node.isScrollable,
                    isEditable = node.isEditable
                )
            )
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectElements(child, elements, maxDepth, currentDepth + 1)
        }
    }

    companion object {
        var instance: AgentAccessibilityService? = null
            private set

        val isConnected: StateFlow<Boolean>
            get() = instance?._isConnected ?: MutableStateFlow(false).asStateFlow()

        val currentPackage: StateFlow<String?>
            get() = instance?._currentPackage ?: MutableStateFlow<String?>(null).asStateFlow()
    }
}
