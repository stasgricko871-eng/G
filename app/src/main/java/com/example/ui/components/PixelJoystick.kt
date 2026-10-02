package com.example.ui.components

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.VirtualButtonConfig
import com.example.input.InputBus
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PixelJoystick(
    config: VirtualButtonConfig,
    isEditMode: Boolean = false,
    onPositionChanged: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var activeDirection by remember { mutableStateOf(DPadDirection.NONE) }

    val stickOffsetX = remember { Animatable(0f) }
    val stickOffsetY = remember { Animatable(0f) }

    val maxRadiusPx = with(density) { (config.widthDp.dp / 2.6f).toPx() }
    val thumbSizeDp = (config.widthDp * 0.42f).dp

    val interactiveModifier = if (isEditMode) {
        Modifier.pointerInput(config.id) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                onPositionChanged(dragAmount.x, dragAmount.y)
            }
        }
    } else {
        Modifier.pointerInteropFilter { motionEvent ->
            val centerX = config.widthDp * density.density / 2f
            val centerY = config.heightDp * density.density / 2f

            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val rawDx = motionEvent.x - centerX
                    val rawDy = motionEvent.y - centerY
                    val distance = sqrt(rawDx * rawDx + rawDy * rawDy)
                    val angle = atan2(rawDy.toDouble(), rawDx.toDouble())

                    val clampedDist = min(distance, maxRadiusPx)
                    val targetX = (clampedDist * cos(angle)).toFloat()
                    val targetY = (clampedDist * sin(angle)).toFloat()

                    scope.launch {
                        stickOffsetX.snapTo(targetX)
                        stickOffsetY.snapTo(targetY)
                    }

                    // Deadzone 20%
                    if (distance > maxRadiusPx * 0.20f) {
                        val angleDeg = Math.toDegrees(angle).toFloat()
                        val newDir = when {
                            angleDeg in -67.5.. -22.5 -> DPadDirection.UP_RIGHT
                            angleDeg in -112.5.. -67.5 -> DPadDirection.UP
                            angleDeg in -157.5.. -112.5 -> DPadDirection.UP_LEFT
                            angleDeg in 22.5..67.5 -> DPadDirection.DOWN_RIGHT
                            angleDeg in 67.5..112.5 -> DPadDirection.DOWN
                            angleDeg in 112.5..157.5 -> DPadDirection.DOWN_LEFT
                            angleDeg > 157.5 || angleDeg < -157.5 -> DPadDirection.LEFT
                            else -> DPadDirection.RIGHT
                        }

                        if (newDir != activeDirection) {
                            releaseDirectionKeys(activeDirection)
                            activeDirection = newDir
                            pressDirectionKeys(context, newDir)
                        }
                    } else {
                        if (activeDirection != DPadDirection.NONE) {
                            releaseDirectionKeys(activeDirection)
                            activeDirection = DPadDirection.NONE
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    releaseDirectionKeys(activeDirection)
                    activeDirection = DPadDirection.NONE

                    // Return thumb stick to center with a smooth spring
                    scope.launch {
                        stickOffsetX.animateTo(0f, spring(dampingRatio = 0.6f))
                    }
                    scope.launch {
                        stickOffsetY.animateTo(0f, spring(dampingRatio = 0.6f))
                    }
                    true
                }
                else -> false
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(config.widthDp.dp, config.heightDp.dp)
            .alpha(config.opacity)
            .testTag("joystick_controller")
            .then(interactiveModifier)
    ) {
        // Base plate (Placeholder texture)
        Image(
            painter = painterResource(id = R.drawable.texture_joystick_base),
            contentDescription = "Джойстик База",
            modifier = Modifier.fillMaxSize()
        )

        // Draggable thumb knob (Placeholder texture)
        Box(
            modifier = Modifier
                .offset { IntOffset(stickOffsetX.value.toInt(), stickOffsetY.value.toInt()) }
                .size(thumbSizeDp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.texture_joystick_thumb),
                contentDescription = "Джойстик Стик",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun pressDirectionKeys(context: android.content.Context, dir: DPadDirection) {
    when (dir) {
        DPadDirection.UP -> InputBus.onKeyDown(context, "DPAD_UP", KeyEvent.KEYCODE_DPAD_UP)
        DPadDirection.DOWN -> InputBus.onKeyDown(context, "DPAD_DOWN", KeyEvent.KEYCODE_DPAD_DOWN)
        DPadDirection.LEFT -> InputBus.onKeyDown(context, "DPAD_LEFT", KeyEvent.KEYCODE_DPAD_LEFT)
        DPadDirection.RIGHT -> InputBus.onKeyDown(context, "DPAD_RIGHT", KeyEvent.KEYCODE_DPAD_RIGHT)
        DPadDirection.UP_LEFT -> {
            InputBus.onKeyDown(context, "DPAD_UP", KeyEvent.KEYCODE_DPAD_UP)
            InputBus.onKeyDown(context, "DPAD_LEFT", KeyEvent.KEYCODE_DPAD_LEFT)
        }
        DPadDirection.UP_RIGHT -> {
            InputBus.onKeyDown(context, "DPAD_UP", KeyEvent.KEYCODE_DPAD_UP)
            InputBus.onKeyDown(context, "DPAD_RIGHT", KeyEvent.KEYCODE_DPAD_RIGHT)
        }
        DPadDirection.DOWN_LEFT -> {
            InputBus.onKeyDown(context, "DPAD_DOWN", KeyEvent.KEYCODE_DPAD_DOWN)
            InputBus.onKeyDown(context, "DPAD_LEFT", KeyEvent.KEYCODE_DPAD_LEFT)
        }
        DPadDirection.DOWN_RIGHT -> {
            InputBus.onKeyDown(context, "DPAD_DOWN", KeyEvent.KEYCODE_DPAD_DOWN)
            InputBus.onKeyDown(context, "DPAD_RIGHT", KeyEvent.KEYCODE_DPAD_RIGHT)
        }
        DPadDirection.NONE -> {}
    }
}

private fun releaseDirectionKeys(dir: DPadDirection) {
    when (dir) {
        DPadDirection.UP -> InputBus.onKeyUp("DPAD_UP", KeyEvent.KEYCODE_DPAD_UP)
        DPadDirection.DOWN -> InputBus.onKeyUp("DPAD_DOWN", KeyEvent.KEYCODE_DPAD_DOWN)
        DPadDirection.LEFT -> InputBus.onKeyUp("DPAD_LEFT", KeyEvent.KEYCODE_DPAD_LEFT)
        DPadDirection.RIGHT -> InputBus.onKeyUp("DPAD_RIGHT", KeyEvent.KEYCODE_DPAD_RIGHT)
        DPadDirection.UP_LEFT -> {
            InputBus.onKeyUp("DPAD_UP", KeyEvent.KEYCODE_DPAD_UP)
            InputBus.onKeyUp("DPAD_LEFT", KeyEvent.KEYCODE_DPAD_LEFT)
        }
        DPadDirection.UP_RIGHT -> {
            InputBus.onKeyUp("DPAD_UP", KeyEvent.KEYCODE_DPAD_UP)
            InputBus.onKeyUp("DPAD_RIGHT", KeyEvent.KEYCODE_DPAD_RIGHT)
        }
        DPadDirection.DOWN_LEFT -> {
            InputBus.onKeyUp("DPAD_DOWN", KeyEvent.KEYCODE_DPAD_DOWN)
            InputBus.onKeyUp("DPAD_LEFT", KeyEvent.KEYCODE_DPAD_LEFT)
        }
        DPadDirection.DOWN_RIGHT -> {
            InputBus.onKeyUp("DPAD_DOWN", KeyEvent.KEYCODE_DPAD_DOWN)
            InputBus.onKeyUp("DPAD_RIGHT", KeyEvent.KEYCODE_DPAD_RIGHT)
        }
        DPadDirection.NONE -> {}
    }
}
