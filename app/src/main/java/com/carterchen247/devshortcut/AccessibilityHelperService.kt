package com.carterchen247.devshortcut

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout
import java.util.ArrayDeque
import java.util.Deque


class AccessibilityHelperService : AccessibilityService() {

    private lateinit var mLayout: FrameLayout

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
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
    }

    private fun configureScrollButton() {
        val scrollButton = mLayout.findViewById<View>(R.id.scroll) as Button
        scrollButton.setOnClickListener {
            scroll()
        }
    }

    private fun scroll() {
        val scrollableNode = findScrollableNode(rootInActiveWindow) ?: return
        scrollableNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
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
}