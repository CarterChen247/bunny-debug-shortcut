package com.carterchen247.devshortcut

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent


class AccessibilityHelperService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }
}