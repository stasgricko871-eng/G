package com.example.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ButtonShape
import com.example.data.model.ButtonStyle
import com.example.data.model.VirtualButtonConfig
import com.example.data.model.VirtualKey
import com.example.input.InputBus

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PixelVirtualButton(
    config: VirtualButtonConfig,
    isEditMode: Boolean = false,
    onButtonSelected: (VirtualButtonConfig) -> Unit = {},
    onPositionChanged: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPressedState by remember { mutableStateOf(false) }
    val mappedKey = remember(config.keyId) { VirtualKey.findById(config.keyId) }

    val interactiveModifier = if (isEditMode) {
        Modifier
            .pointerInput(config.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Drag is handled by parent container with absolute offsets or drag delta
                    onPositionChanged(dragAmount.x, dragAmount.y)
                }
            }
            .pointerInput(config.id + "_click") {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.isConsumed }) continue
                    }
                }
            }
    } else {
        Modifier.pointerInteropFilter { motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    isPressedState = true
                    InputBus.onKeyDown(context, config.keyId, mappedKey.keyCode)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    isPressedState = false
                    InputBus.onKeyUp(config.keyId, mappedKey.keyCode)
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
            .testTag("button_${config.label.lowercase()}")
            .then(interactiveModifier)
    ) {
        if (config.shape == ButtonShape.CIRCLE && !isPressedState) {
            // Подключаемая текстура круглой кнопки (заменяется в res/drawable/texture_button_round)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.texture_button_round),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRetroButton(
                shape = config.shape,
                style = config.style,
                isPressed = isPressedState,
                isEditMode = isEditMode
            )
        }

        // Button Label
        val textColor = when {
            isPressedState -> Color.Black
            config.style == ButtonStyle.TRANSPARENT -> Color.White.copy(alpha = 0.9f)
            else -> Color.White
        }

        Text(
            text = config.label,
            color = textColor,
            fontSize = if (config.label.length > 2) 13.sp else 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

private fun DrawScope.drawRetroButton(
    shape: ButtonShape,
    style: ButtonStyle,
    isPressed: Boolean,
    isEditMode: Boolean
) {
    val strokeWidth = 3.dp.toPx()
    val pixelStep = 4.dp.toPx()

    val fillColor = when {
        isPressed -> Color.White
        style == ButtonStyle.TRANSPARENT -> Color.Black.copy(alpha = 0.25f)
        style == ButtonStyle.OUTLINE -> Color.Transparent
        else -> Color.Black.copy(alpha = 0.85f)
    }

    val borderColor = when {
        isEditMode -> Color(0xFF00E5FF) // Cyan highlight in edit mode
        isPressed -> Color.White
        style == ButtonStyle.MINIMAL -> Color(0xFFCCCCCC)
        style == ButtonStyle.TRANSPARENT -> Color.White.copy(alpha = 0.6f)
        else -> Color.White
    }

    when (shape) {
        ButtonShape.CIRCLE -> {
            val radius = (size.minDimension / 2f) - (strokeWidth / 2f)
            val center = Offset(size.width / 2f, size.height / 2f)

            // Fill
            drawCircle(color = fillColor, radius = radius, center = center)

            // Pixel art outer stepped ring or clean stroke
            if (style == ButtonStyle.PIXEL) {
                drawCircle(color = borderColor, radius = radius, center = center, style = Stroke(width = strokeWidth))
                // Inner pixel shadow
                if (!isPressed) {
                    drawCircle(
                        color = Color.DarkGray,
                        radius = radius - strokeWidth,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                }
            } else {
                drawCircle(color = borderColor, radius = radius, center = center, style = Stroke(width = strokeWidth))
            }
        }

        ButtonShape.SQUARE -> {
            val rectSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            drawRect(color = fillColor, topLeft = topLeft, size = rectSize)
            drawRect(color = borderColor, topLeft = topLeft, size = rectSize, style = Stroke(width = strokeWidth))

            if (style == ButtonStyle.PIXEL && !isPressed) {
                // Pixelated drop-shadow edge
                drawLine(
                    color = Color.Gray,
                    start = Offset(topLeft.x + pixelStep, topLeft.y + rectSize.height - 2),
                    end = Offset(topLeft.x + rectSize.width - 2, topLeft.y + rectSize.height - 2),
                    strokeWidth = 3f
                )
            }
        }

        ButtonShape.ROUNDED_RECT -> {
            val rectSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())

            drawRoundRect(color = fillColor, topLeft = topLeft, size = rectSize, cornerRadius = cornerRadius)
            drawRoundRect(
                color = borderColor,
                topLeft = topLeft,
                size = rectSize,
                cornerRadius = cornerRadius,
                style = Stroke(width = strokeWidth)
            )
        }

        ButtonShape.DIAMOND -> {
            val path = Path().apply {
                moveTo(size.width / 2f, strokeWidth)
                lineTo(size.width - strokeWidth, size.height / 2f)
                lineTo(size.width / 2f, size.height - strokeWidth)
                lineTo(strokeWidth, size.height / 2f)
                close()
            }
            drawPath(path = path, color = fillColor, style = Fill)
            drawPath(path = path, color = borderColor, style = Stroke(width = strokeWidth))
        }

        ButtonShape.DPAD_CROSS -> {
            // DPadCross is rendered by PixelDPad component
        }
    }
}
