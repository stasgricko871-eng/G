package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * VolumeKeyAccessibilityService intercepts hardware Volume Up button presses globally.
 *
 * Android Security & API Compliance:
 * Standard Android applications and overlay windows cannot globally intercept hardware volume
 * key presses outside their own window. Android's official and permitted mechanism for global
 * key filtering is through an AccessibilityService with [AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS].
 *
 * When enabled by the user in system settings:
 * 1. Volume Up DOWN (repeatCount == 0) toggles overlay visibility and returns true to suppress
 *    media volume changes.
 * 2. Volume Down returns false, allowing normal volume decrease without interference.
 * 3. No root, ADB hacks, or hidden APIs are used.
 */
class VolumeKeyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.apply {
            eventTypes = 0
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            notificationTimeout = 100
        }
        serviceInfo = info
        instance = this
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // Only toggle on the initial down press, ignoring repeat count while held
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (event.repeatCount == 0) {
                    OverlayService.toggleVisibility(this)
                }
                // Returning true consumes the event and suppresses Android volume increase
                return true
            } else if (event.action == KeyEvent.ACTION_UP) {
                return true
            }
        }
        // Let Volume Down and all other keys proceed normally
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No UI accessibility events needed for key filtering
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    companion object {
        var instance: VolumeKeyAccessibilityService? = null
            private set
    }
}
