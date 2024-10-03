package com.carterchen247.devshortcut

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import java.util.ArrayDeque


class AccessibilityHelperService : AccessibilityService() {

    companion object {
        private const val TARGET_TEXT = "USB"
        private const val INTERVAL_SCROLL = 200L
    }

    private lateinit var overlayLayout: FrameLayout
    private val validTitles = ValidTitle.entries.map { it.string }

    private var isTargetVisible = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var scrollJob: Job? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> scrollJob?.cancel()
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> handleWindowStateChanged(event)
            else -> {
                // do nothing
            }
        }
    }

    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        if (event.className?.startsWith("android.widget.") == true) {
            return
        }
        overlayLayout.isVisible =
            event.packageName == "com.android.settings" && event.text.any { it in validTitles }
    }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        setupOverlayLayout()
        configureScrollButton()
        launchDeveloperSettings()
    }

    private fun setupOverlayLayout() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayLayout = FrameLayout(this)
        val lp = WindowManager.LayoutParams()
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        lp.format = PixelFormat.TRANSLUCENT
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP or Gravity.END
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.action_bar, overlayLayout)
        windowManager.addView(overlayLayout, lp)
    }

    private fun configureScrollButton() {
        overlayLayout.findViewById<View>(R.id.scroll).setOnClickListener {
            scrollToTarget(TARGET_TEXT)
        }
    }

    private fun launchDeveloperSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun scrollToTarget(target: String) {
        isTargetVisible = false
        scrollJob?.cancel()
        scrollJob = coroutineScope.launch {
            scrollToTargetInternal(target)
        }
    }

    private suspend fun scrollToTargetInternal(target: String) {
        if (isTargetVisible) {
            return
        }
        val node = findNodeByText(rootInActiveWindow, target)
        if (node != null) {
            isTargetVisible = true
            return
        }

        findScrollableNode(rootInActiveWindow)?.let { scrollableNode ->
            scrollableNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
            delay(INTERVAL_SCROLL)
            scrollToTarget(target)
        }
    }

    /**
     * copy from GlobalActionBarService https://github.com/android/codelab-android-accessibility/tree/master/GlobalActionBarService
     */
    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val deque = ArrayDeque<AccessibilityNodeInfo>().apply { add(root) }
        while (deque.isNotEmpty()) {
            val node = deque.removeFirst()
            if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node
            }
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                deque.addLast(child)
            }
        }
        return null
    }

    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            if (child.text?.toString()?.contains(text, ignoreCase = true) == true) {
                return child
            }
            val result = findNodeByText(child, text)
            if (result != null) return result
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}