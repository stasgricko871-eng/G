package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ProfileRepository
import com.example.input.InputBus
import com.example.ui.components.PixelDPad
import com.example.ui.components.PixelVirtualButton
import kotlinx.coroutines.delay

@Composable
fun TestControllerScreen(
    profileRepository: ProfileRepository,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val activeProfile by profileRepository.activeProfileFlow.collectAsState()
    val pressedKeys by InputBus.pressedKeysFlow.collectAsState()

    // Character position in pixels inside the arena
    var charX by remember { mutableFloatStateOf(0f) }
    var charY by remember { mutableFloatStateOf(0f) }
    var isInitialized by remember { mutableStateOf(false) }

    // Visual actions
    var actionEffectText by remember { mutableStateOf<String?>(null) }
    var actionEffectColor by remember { mutableStateOf(Color.White) }
    val glowAnim = remember { Animatable(0f) }

    // Game loop for smooth character movement when D-Pad is pressed
    LaunchedEffect(pressedKeys) {
        val speed = if (pressedKeys.contains("X")) 8f else 4.5f // Holding X runs faster!

        while (pressedKeys.any { it.startsWith("DPAD") }) {
            var dx = 0f
            var dy = 0f

            if (pressedKeys.contains("DPAD_UP")) dy -= speed
            if (pressedKeys.contains("DPAD_DOWN")) dy += speed
            if (pressedKeys.contains("DPAD_LEFT")) dx -= speed
            if (pressedKeys.contains("DPAD_RIGHT")) dx += speed

            charX += dx
            charY += dy

            delay(16) // ~60fps
        }
    }

    // Reaction to Z (Interact/Slash), X (Dash), C (Menu)
    LaunchedEffect(pressedKeys) {
        if (pressedKeys.contains("Z")) {
            actionEffectText = "★ Z: ДЕЙСТВИЕ / УДАР ★"
            actionEffectColor = Color(0xFF4AE290)
            glowAnim.snapTo(1f)
            glowAnim.animateTo(0f, tween(300))
        } else if (pressedKeys.contains("X")) {
            actionEffectText = "⚡ X: РЫВОК / УСКОРЕНИЕ ⚡"
            actionEffectColor = Color(0xFF00E5FF)
            glowAnim.snapTo(1f)
            glowAnim.animateTo(0f, tween(300))
        } else if (pressedKeys.contains("C")) {
            actionEffectText = "✦ C: МЕНЮ / ИНВЕНТАРЬ ✦"
            actionEffectColor = Color(0xFFFFCC00)
            glowAnim.snapTo(1f)
            glowAnim.animateTo(0f, tween(300))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
            .padding(top = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E1E2A))
                    .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                    .testTag("back_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "ТЕСТ УПРАВЛЕНИЯ",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Игровая арена и Debug Panel мультитача",
                    color = Color(0xFF4AE290),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Debug Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF14141E))
                .border(1.5.dp, Color.White, RoundedCornerShape(4.dp))
                .padding(10.dp)
                .testTag("debug_panel")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DEBUG: НАЖАТЫЕ КЛАВИШИ",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Мультитач: ${pressedKeys.size} кл.",
                        color = if (pressedKeys.size > 1) Color(0xFF4AE290) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val debugKeyIds = listOf(
                        "DPAD_UP" to "↑ UP",
                        "DPAD_DOWN" to "↓ DN",
                        "DPAD_LEFT" to "← LF",
                        "DPAD_RIGHT" to "→ RT",
                        "Z" to "Z",
                        "X" to "X",
                        "C" to "C"
                    )

                    debugKeyIds.forEach { (id, label) ->
                        val isDown = pressedKeys.contains(id)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isDown) Color(0xFF4AE290) else Color(0xFF22222E))
                                .border(1.dp, if (isDown) Color.White else Color(0xFF3E3E4C), RoundedCornerShape(2.dp))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isDown) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Game Arena (Top half)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF08080C))
                .border(2.dp, Color.White, RoundedCornerShape(4.dp))
        ) {
            val arenaWidth = constraints.maxWidth.toFloat()
            val arenaHeight = constraints.maxHeight.toFloat()

            if (!isInitialized && arenaWidth > 0 && arenaHeight > 0) {
                charX = arenaWidth / 2f
                charY = arenaHeight / 2f
                isInitialized = true
            }

            // Keep character inside arena
            charX = charX.coerceIn(24f, (arenaWidth - 24f).coerceAtLeast(24f))
            charY = charY.coerceIn(24f, (arenaHeight - 24f).coerceAtLeast(24f))

            // Background Retro Grid Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridStep = 32.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color(0xFF14141E),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += gridStep
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color(0xFF14141E),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += gridStep
                }
            }

            // Retro Character / Hero Heart Marker
            Box(
                modifier = Modifier
                    .offset { IntOffset((charX - 18.dp.toPx()).toInt(), (charY - 18.dp.toPx()).toInt()) }
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRetroCharacterHeart(
                        isAction = glowAnim.value > 0.05f,
                        actionColor = actionEffectColor
                    )
                }
            }

            // Action effect banner
            if (actionEffectText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .border(1.dp, actionEffectColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = actionEffectText ?: "",
                        color = actionEffectColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // On-Screen Virtual Controller Area (Bottom half)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .background(Color(0xFF0F0F16))
                .border(1.dp, Color(0xFF262636))
        ) {
            val screenWidthPx = constraints.maxWidth.toFloat()
            val screenHeightPx = constraints.maxHeight.toFloat()
            val density = LocalDensity.current

            activeProfile.buttons.forEach { button ->
                val buttonWidthPx = with(density) { button.widthDp.dp.toPx() }
                val buttonHeightPx = with(density) { button.heightDp.dp.toPx() }

                val posX = (button.xFraction * screenWidthPx) - (buttonWidthPx / 2f)
                val posY = (button.yFraction * screenHeightPx) - (buttonHeightPx / 2f)

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = posX.coerceIn(0f, (screenWidthPx - buttonWidthPx).coerceAtLeast(0f)).toInt(),
                                y = posY.coerceIn(0f, (screenHeightPx - buttonHeightPx).coerceAtLeast(0f)).toInt()
                            )
                        }
                ) {
                    if (button.isJoystick) {
                        com.example.ui.components.PixelJoystick(config = button)
                    } else if (button.isDPad) {
                        PixelDPad(config = button)
                    } else {
                        PixelVirtualButton(config = button)
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRetroCharacterHeart(
    isAction: Boolean,
    actionColor: Color
) {
    val w = size.width
    val h = size.height

    // Original retro pixel RPG heart silhouette
    val heartColor = if (isAction) actionColor else Color(0xFFFF3366)
    val borderColor = Color.White

    val path = Path().apply {
        moveTo(w / 2f, h * 0.9f)
        cubicTo(w * 0.1f, h * 0.6f, 0f, h * 0.25f, w * 0.25f, h * 0.1f)
        cubicTo(w * 0.4f, 0f, w / 2f, h * 0.2f, w / 2f, h * 0.3f)
        cubicTo(w / 2f, h * 0.2f, w * 0.6f, 0f, w * 0.75f, h * 0.1f)
        cubicTo(w, h * 0.25f, w * 0.9f, h * 0.6f, w / 2f, h * 0.9f)
        close()
    }

    drawPath(path = path, color = heartColor, style = Fill)
    drawPath(path = path, color = borderColor, style = Stroke(width = 2.5f))

    // Pixel highlight
    drawRect(
        color = Color.White.copy(alpha = 0.8f),
        topLeft = Offset(w * 0.25f, h * 0.25f),
        size = Size(4f, 6f)
    )
}
