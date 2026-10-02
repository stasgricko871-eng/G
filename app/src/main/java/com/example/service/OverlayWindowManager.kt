package com.example.service

import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.model.ControllerProfile
import com.example.data.repository.ProfileRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.components.PixelDPad
import com.example.ui.components.PixelVirtualButton
import com.example.ui.overlay.OverlayControllerView
import com.example.ui.theme.MyApplicationTheme

/**
 * Manages floating overlay views on top of other apps.
 *
 * Click-Through & Game Touch Architecture:
 * - In Normal Gameplay Mode: Each button and the D-Pad occupies only its own small WindowManager
 *   window matching its exact width & height (WRAP_CONTENT bounds).
 *   The entire remainder of the screen (90%+) has NO window over it, meaning 100% of touches in
 *   the game, browser, or underlying apps pass through natively without any interference or blocking!
 * - In Edit Mode: A full-screen interactive editor window is shown, allowing the user to freely
 *   drag buttons, resize them, add custom buttons, and tap "Готово" to return to gameplay mode.
 */
class OverlayWindowManager(
    private val context: Context,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val onCloseRequest: () -> Unit
) : LifecycleOwner, SavedStateRegistryOwner {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    // Views in normal gameplay mode (per-button lightweight windows)
    private val activeNormalViews = mutableListOf<View>()

    // Floating badge view (allows opening edit mode or hiding with one tap)
    private var floatingBadgeView: View? = null

    // Full-screen view only used during Edit Mode
    private var editModeComposeView: View? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

    init {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    /**
     * Updates the overlay layout depending on visibility, edit mode, and active profile.
     */
    fun updateLayout(visible: Boolean, isEditMode: Boolean, profile: ControllerProfile) {
        if (!visible) {
            removeAllViews()
            return
        }

        if (isEditMode) {
            // Remove per-button windows, show full-screen interactive editor
            removeNormalViews()
            showEditModeWindow()
        } else {
            // Remove full-screen editor, show lightweight per-button windows
            removeEditModeWindow()
            showNormalGameplayWindows(profile)
        }
    }

    private fun showNormalGameplayWindows(profile: ControllerProfile) {
        removeNormalViews()

        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val density = displayMetrics.density

        // Create independent window for each button in the profile
        for (button in profile.buttons) {
            val widthPx = (button.widthDp * density).toInt()
            val heightPx = (button.heightDp * density).toInt()

            val posX = ((button.xFraction * screenWidth) - (widthPx / 2f)).toInt()
                .coerceIn(0, (screenWidth - widthPx).coerceAtLeast(0))
            val posY = ((button.yFraction * screenHeight) - (heightPx / 2f)).toInt()
                .coerceIn(0, (screenHeight - heightPx).coerceAtLeast(0))

            val params = WindowManager.LayoutParams(
                widthPx,
                heightPx,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = posX
                y = posY
            }

            val view = ComposeView(context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
                setViewTreeLifecycleOwner(this@OverlayWindowManager)
                setViewTreeSavedStateRegistryOwner(this@OverlayWindowManager)
                setContent {
                    MyApplicationTheme(darkTheme = true) {
                        if (button.isJoystick) {
                            com.example.ui.components.PixelJoystick(
                                config = button.copy(opacity = button.opacity * profile.globalOpacity)
                            )
                        } else if (button.isDPad) {
                            PixelDPad(
                                config = button.copy(opacity = button.opacity * profile.globalOpacity)
                            )
                        } else {
                            PixelVirtualButton(
                                config = button.copy(opacity = button.opacity * profile.globalOpacity)
                            )
                        }
                    }
                }
            }

            try {
                windowManager.addView(view, params)
                activeNormalViews.add(view)
            } catch (_: Exception) {
            }
        }

        // Add small floating badge in top-right corner to easily toggle Edit Mode
        showFloatingBadge(screenWidth, density)
    }

    private fun showFloatingBadge(screenWidth: Int, density: Float) {
        floatingBadgeView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
            floatingBadgeView = null
        }

        val badgeSizePx = (38 * density).toInt()
        val badgeX = screenWidth - badgeSizePx - (12 * density).toInt()
        val badgeY = (36 * density).toInt()

        val params = WindowManager.LayoutParams(
            badgeSizePx,
            badgeSizePx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = badgeX
            y = badgeY
        }

        val badgeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setViewTreeLifecycleOwner(this@OverlayWindowManager)
            setViewTreeSavedStateRegistryOwner(this@OverlayWindowManager)
            setContent {
                MyApplicationTheme(darkTheme = true) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF14141E).copy(alpha = 0.85f))
                            .border(1.5.dp, Color(0xFF00E5FF), CircleShape)
                            .clickable {
                                settingsRepository.setEditMode(true)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Редактировать управление",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        try {
            windowManager.addView(badgeView, params)
            floatingBadgeView = badgeView
        } catch (_: Exception) {
            floatingBadgeView = null
        }
    }

    private fun showEditModeWindow() {
        if (editModeComposeView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setViewTreeLifecycleOwner(this@OverlayWindowManager)
            setViewTreeSavedStateRegistryOwner(this@OverlayWindowManager)
            setContent {
                MyApplicationTheme(darkTheme = true) {
                    OverlayControllerView(
                        profileRepository = profileRepository,
                        settingsRepository = settingsRepository,
                        onCloseOverlay = onCloseRequest
                    )
                }
            }
        }

        try {
            windowManager.addView(composeView, params)
            editModeComposeView = composeView
        } catch (_: Exception) {
            editModeComposeView = null
        }
    }

    private fun removeNormalViews() {
        for (view in activeNormalViews) {
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {
            }
        }
        activeNormalViews.clear()

        floatingBadgeView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
            floatingBadgeView = null
        }
    }

    private fun removeEditModeWindow() {
        editModeComposeView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
            editModeComposeView = null
        }
    }

    fun removeAllViews() {
        removeNormalViews()
        removeEditModeWindow()
    }

    fun destroy() {
        removeAllViews()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}
