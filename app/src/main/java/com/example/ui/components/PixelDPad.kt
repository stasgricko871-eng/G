package com.example.ui.components

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ButtonStyle
import com.example.data.model.VirtualButtonConfig
import com.example.input.InputBus
import kotlin.math.atan2
import kotlin.math.sqrt

enum class DPadDirection {
    NONE, UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PixelDPad(
    config: VirtualButtonConfig,
    isEditMode: Boolean = false,
    onPositionChanged: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeDirection by remember { mutableStateOf(DPadDirection.NONE) }

    val interactiveModifier = if (isEditMode) {
        Modifier.pointerInput(config.id) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                onPositionChanged(dragAmount.x, dragAmount.y)
            }
        }
    } else {
        Modifier.pointerInteropFilter { motionEvent ->
            val w = motionEvent.x
            val h = motionEvent.y
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val dir = calculateDPadDirection(w, h, config.widthDp.toFloat(), config.heightDp.toFloat())
                    if (dir != activeDirection) {
                        // Release previous keys
                        releaseDirectionKeys(activeDirection)
                        activeDirection = dir
                        // Press new keys
                        pressDirectionKeys(context, dir)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    releaseDirectionKeys(activeDirection)
                    activeDirection = DPadDirection.NONE
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
            .testTag("dpad_controller")
            .then(interactiveModifier)
    ) {
        // Подключаемая текстура крестовины (заменяется в res/drawable/texture_dpad_cross)
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.texture_dpad_cross),
            contentDescription = "Текстура D-Pad",
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRetroCrossDPad(
                style = config.style,
                activeDirection = activeDirection,
                isEditMode = isEditMode
            )
        }
    }
}

private fun calculateDPadDirection(touchX: Float, touchY: Float, widthDp: Float, heightDp: Float): DPadDirection {
    // Convert touch to normalized center [-1..1]
    val cx = widthDp * 1.5f // approximate dp to px or ratio
    val cy = heightDp * 1.5f
    val dx = touchX - (touchX / touchX * 0.5f) // relative
    // Simple 3x3 grid detection
    val normX = touchX / (touchX.coerceAtLeast(1f) + (widthDp - touchX).coerceAtLeast(0f))
    // Let's use direct proportion of view dimensions
    val relX = (touchX / (widthDp * 2.5f)) - 0.5f
    val relY = (touchY / (heightDp * 2.5f)) - 0.5f

    val dist = sqrt(relX * relX + relY * relY)
    if (dist < 0.10f) {
        // Dead center
        return DPadDirection.NONE
    }

    // Angle in degrees -180..180
    val angle = Math.toDegrees(atan2(relY.toDouble(), relX.toDouble())).toFloat()

    return when {
        angle in -67.5.. -22.5 -> DPadDirection.UP_RIGHT
        angle in -112.5.. -67.5 -> DPadDirection.UP
        angle in -157.5.. -112.5 -> DPadDirection.UP_LEFT
        angle in 22.5..67.5 -> DPadDirection.DOWN_RIGHT
        angle in 67.5..112.5 -> DPadDirection.DOWN
        angle in 112.5..157.5 -> DPadDirection.DOWN_LEFT
        angle > 157.5 || angle < -157.5 -> DPadDirection.LEFT
        else -> DPadDirection.RIGHT
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

private fun DrawScope.drawRetroCrossDPad(
    style: ButtonStyle,
    activeDirection: DPadDirection,
    isEditMode: Boolean
) {
    val w = size.width
    val h = size.height
    val strokeWidth = 3.dp.toPx()

    val armWidth = w / 3f
    val armHeight = h / 3f

    val baseFill = when (style) {
        ButtonStyle.TRANSPARENT -> Color.Black.copy(alpha = 0.35f)
        ButtonStyle.OUTLINE -> Color.Transparent
        else -> Color.Black.copy(alpha = 0.88f)
    }

    val borderColor = when {
        isEditMode -> Color(0xFF00E5FF)
        style == ButtonStyle.MINIMAL -> Color(0xFFDDDDDD)
        style == ButtonStyle.TRANSPARENT -> Color.White.copy(alpha = 0.6f)
        else -> Color.White
    }

    val activeFill = Color(0xFF4AE290) // Retro neon mint/green on press

    // Draw Cross polygon path
    val crossPath = Path().apply {
        moveTo(armWidth, 0f)
        lineTo(armWidth * 2f, 0f)
        lineTo(armWidth * 2f, armHeight)
        lineTo(w, armHeight)
        lineTo(w, armHeight * 2f)
        lineTo(armWidth * 2f, armHeight * 2f)
        lineTo(armWidth * 2f, h)
        lineTo(armWidth, h)
        lineTo(armWidth, armHeight * 2f)
        lineTo(0f, armHeight * 2f)
        lineTo(0f, armHeight)
        lineTo(armWidth, armHeight)
        close()
    }

    // Fill background
    drawPath(path = crossPath, color = baseFill, style = Fill)

    // Highlight active arms
    val isUp = activeDirection == DPadDirection.UP || activeDirection == DPadDirection.UP_LEFT || activeDirection == DPadDirection.UP_RIGHT
    val isDown = activeDirection == DPadDirection.DOWN || activeDirection == DPadDirection.DOWN_LEFT || activeDirection == DPadDirection.DOWN_RIGHT
    val isLeft = activeDirection == DPadDirection.LEFT || activeDirection == DPadDirection.UP_LEFT || activeDirection == DPadDirection.DOWN_LEFT
    val isRight = activeDirection == DPadDirection.RIGHT || activeDirection == DPadDirection.UP_RIGHT || activeDirection == DPadDirection.DOWN_RIGHT

    if (isUp) {
        drawRect(color = activeFill, topLeft = Offset(armWidth, 0f), size = Size(armWidth, armHeight))
    }
    if (isDown) {
        drawRect(color = activeFill, topLeft = Offset(armWidth, armHeight * 2f), size = Size(armWidth, armHeight))
    }
    if (isLeft) {
        drawRect(color = activeFill, topLeft = Offset(0f, armHeight), size = Size(armWidth, armHeight))
    }
    if (isRight) {
        drawRect(color = activeFill, topLeft = Offset(armWidth * 2f, armHeight), size = Size(armWidth, armHeight))
    }

    // Center pivot
    drawCircle(
        color = if (activeDirection == DPadDirection.NONE) Color(0xFF222226) else activeFill.copy(alpha = 0.6f),
        radius = armWidth * 0.25f,
        center = Offset(w / 2f, h / 2f)
    )

    // Draw Cross Outline
    drawPath(path = crossPath, color = borderColor, style = Stroke(width = strokeWidth))

    // Draw 4 Arrows: UP, DOWN, LEFT, RIGHT
    val arrowColor = Color.White
    val arrowSize = armWidth * 0.35f

    // UP Arrow
    val upCenter = Offset(w / 2f, armHeight * 0.45f)
    val upPath = Path().apply {
        moveTo(upCenter.x, upCenter.y - arrowSize * 0.7f)
        lineTo(upCenter.x + arrowSize, upCenter.y + arrowSize * 0.5f)
        lineTo(upCenter.x - arrowSize, upCenter.y + arrowSize * 0.5f)
        close()
    }
    drawPath(path = upPath, color = if (isUp) Color.Black else arrowColor, style = Fill)

    // DOWN Arrow
    val downCenter = Offset(w / 2f, armHeight * 2.55f)
    val downPath = Path().apply {
        moveTo(downCenter.x, downCenter.y + arrowSize * 0.7f)
        lineTo(downCenter.x + arrowSize, downCenter.y - arrowSize * 0.5f)
        lineTo(downCenter.x - arrowSize, downCenter.y - arrowSize * 0.5f)
        close()
    }
    drawPath(path = downPath, color = if (isDown) Color.Black else arrowColor, style = Fill)

    // LEFT Arrow
    val leftCenter = Offset(armWidth * 0.45f, h / 2f)
    val leftPath = Path().apply {
        moveTo(leftCenter.x - arrowSize * 0.7f, leftCenter.y)
        lineTo(leftCenter.x + arrowSize * 0.5f, leftCenter.y + arrowSize)
        lineTo(leftCenter.x + arrowSize * 0.5f, leftCenter.y - arrowSize)
        close()
    }
    drawPath(path = leftPath, color = if (isLeft) Color.Black else arrowColor, style = Fill)

    // RIGHT Arrow
    val rightCenter = Offset(armWidth * 2.55f, h / 2f)
    val rightPath = Path().apply {
        moveTo(rightCenter.x + arrowSize * 0.7f, rightCenter.y)
        lineTo(rightCenter.x - arrowSize * 0.5f, rightCenter.y + arrowSize)
        lineTo(rightCenter.x - arrowSize * 0.5f, rightCenter.y - arrowSize)
        close()
    }
    drawPath(path = rightPath, color = if (isRight) Color.Black else arrowColor, style = Fill)
}
