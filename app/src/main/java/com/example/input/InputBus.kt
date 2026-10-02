package com.example.input

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe global input event bus supporting multi-touch key press tracking.
 * Manages KEY_DOWN and KEY_UP states cleanly without simulated root injection.
 */
object InputBus {

    // Currently pressed key IDs (e.g., "DPAD_UP", "Z", "X")
    private val activeKeysMap = ConcurrentHashMap<String, Int>()

    private val _pressedKeysFlow = MutableStateFlow<Set<String>>(emptySet())
    val pressedKeysFlow = _pressedKeysFlow.asStateFlow()

    // Listener callbacks for in-app test area / local gameplay
    private val keyListeners = mutableListOf<(keyId: String, keyCode: Int, isDown: Boolean) -> Unit>()

    var hapticFeedbackEnabled: Boolean = true

    fun addListener(listener: (keyId: String, keyCode: Int, isDown: Boolean) -> Unit) {
        synchronized(keyListeners) {
            keyListeners.add(listener)
        }
    }

    fun removeListener(listener: (keyId: String, keyCode: Int, isDown: Boolean) -> Unit) {
        synchronized(keyListeners) {
            keyListeners.remove(listener)
        }
    }

    fun onKeyDown(context: Context?, keyId: String, keyCode: Int) {
        val wasPressed = activeKeysMap.containsKey(keyId)
        activeKeysMap[keyId] = keyCode
        _pressedKeysFlow.value = activeKeysMap.keys.toSet()

        if (!wasPressed) {
            if (hapticFeedbackEnabled && context != null) {
                performHapticFeedback(context)
            }
            notifyListeners(keyId, keyCode, true)
        }
    }

    fun onKeyUp(keyId: String, keyCode: Int) {
        if (activeKeysMap.remove(keyId) != null) {
            _pressedKeysFlow.value = activeKeysMap.keys.toSet()
            notifyListeners(keyId, keyCode, false)
        }
    }

    fun releaseAll() {
        val keys = activeKeysMap.keys.toList()
        activeKeysMap.clear()
        _pressedKeysFlow.value = emptySet()
        for (k in keys) {
            notifyListeners(k, 0, false)
        }
    }

    fun isKeyPressed(keyId: String): Boolean {
        return activeKeysMap.containsKey(keyId)
    }

    private fun notifyListeners(keyId: String, keyCode: Int, isDown: Boolean) {
        val copy = synchronized(keyListeners) { keyListeners.toList() }
        for (listener in copy) {
            listener(keyId, keyCode, isDown)
        }
    }

    private fun performHapticFeedback(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(20)
                }
            }
        } catch (_: Exception) {
            // Ignore vibration failure if permission denied or hardware missing
        }
    }
}
