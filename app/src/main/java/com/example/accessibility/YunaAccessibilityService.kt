package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class YunaAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        _serviceInstance = this
        _isServiceActive.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Limited inspection for responsive user-driven assistance
    }

    override fun onInterrupt() {
        _isServiceActive.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (_serviceInstance == this) {
            _serviceInstance = null
            _isServiceActive.value = false
        }
    }

    fun executeBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun executeHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun executeNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

    fun findAndClickByText(query: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val matchedNodes = rootNode.findAccessibilityNodeInfosByText(query)
        for (node in matchedNodes) {
            var clickableNode: AccessibilityNodeInfo? = node
            while (clickableNode != null && !clickableNode.isClickable) {
                clickableNode = clickableNode.parent
            }
            if (clickableNode != null && clickableNode.isClickable) {
                val success = clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (success) return true
            }
        }
        return false
    }

    fun scroll(forward: Boolean): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val action = if (forward) {
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        } else {
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        }
        return performScrollRecursive(rootNode, action)
    }

    private fun performScrollRecursive(node: AccessibilityNodeInfo, action: Int): Boolean {
        val actions = node.actionList
        if (actions.any { it.id == action }) {
            return node.performAction(action)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (performScrollRecursive(child, action)) {
                return true
            }
        }
        return false
    }

    fun readVisibleText(): List<String> {
        val rootNode = rootInActiveWindow ?: return emptyList()
        val textList = mutableListOf<String>()
        collectTextRecursive(rootNode, textList)
        return textList
    }

    private fun collectTextRecursive(node: AccessibilityNodeInfo, results: MutableList<String>) {
        node.text?.toString()?.trim()?.let {
            if (it.isNotEmpty() && !results.contains(it)) {
                results.add(it)
            }
        }
        node.contentDescription?.toString()?.trim()?.let {
            if (it.isNotEmpty() && !results.contains(it)) {
                results.add(it)
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectTextRecursive(child, results)
        }
    }

    fun tapScreenCoordinates(x: Float, y: Float): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    companion object {
        @Volatile
        private var _serviceInstance: YunaAccessibilityService? = null
        val serviceInstance: YunaAccessibilityService? get() = _serviceInstance

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        fun isConnected(): Boolean = _serviceInstance != null
    }
}
