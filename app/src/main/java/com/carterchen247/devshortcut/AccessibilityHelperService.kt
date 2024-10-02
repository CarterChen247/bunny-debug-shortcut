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
import android.widget.Button
import android.widget.FrameLayout
import kotlinx.coroutines.*
import java.util.ArrayDeque
import java.util.Deque


class AccessibilityHelperService : AccessibilityService() {

    companion object {
        private const val TARGET_TEXT = "USB"
        private const val INTERVAL_SCROLL = 200L
    }

    private lateinit var mLayout: FrameLayout

    private var isTargetVisible = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var scrollJob: Job? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                scrollJob?.cancel()
            }

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {

            }

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {

            }

            else -> {
                // do nothing
            }
        }
    }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        /**
         * copy from GlobalActionBarService https://github.com/android/codelab-android-accessibility/tree/master/GlobalActionBarService
         */
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        mLayout = FrameLayout(this)
        val lp = WindowManager.LayoutParams()
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        lp.format = PixelFormat.TRANSLUCENT
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.action_bar, mLayout)
        wm.addView(mLayout, lp)

        configureScrollButton()

        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun configureScrollButton() {
        val scrollButton = mLayout.findViewById<View>(R.id.scroll) as Button
        scrollButton.setOnClickListener {
            scrollToTarget(TARGET_TEXT)
        }
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

        val scrollableNode = findScrollableNode(rootInActiveWindow) ?: return
        scrollableNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
        delay(INTERVAL_SCROLL)
        scrollToTarget(target)
    }

    /**
     * copy from GlobalActionBarService https://github.com/android/codelab-android-accessibility/tree/master/GlobalActionBarService
     */
    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        while (!deque.isEmpty()) {
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