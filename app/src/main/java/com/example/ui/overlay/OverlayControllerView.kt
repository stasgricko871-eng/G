package com.example.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ButtonShape
import com.example.data.model.ButtonStyle
import com.example.data.model.ControllerProfile
import com.example.data.model.VirtualButtonConfig
import com.example.data.repository.ProfileRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.components.ButtonEditDialog
import com.example.ui.components.PixelDPad
import com.example.ui.components.PixelVirtualButton
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun OverlayControllerView(
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    onCloseOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProfile by profileRepository.activeProfileFlow.collectAsState()
    val isEditMode by settingsRepository.isEditMode.collectAsState()
    val isOverlayVisible by settingsRepository.isOverlayVisible.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedButtonForEdit by remember { mutableStateOf<VirtualButtonConfig?>(null) }

    // If overlay is hidden via Volume Up or toggle, show nothing or only a discreet toggle pill
    if (!isOverlayVisible) {
        return
    }

    if (selectedButtonForEdit != null) {
        ButtonEditDialog(
            buttonConfig = selectedButtonForEdit!!,
            onSave = { updated ->
                val currentButtons = activeProfile.buttons.toMutableList()
                val idx = currentButtons.indexOfFirst { it.id == updated.id }
                if (idx >= 0) {
                    currentButtons[idx] = updated
                }
                scope.launch {
                    profileRepository.updateActiveProfileButtons(currentButtons)
                }
                selectedButtonForEdit = null
            },
            onDuplicate = { orig ->
                val duplicate = orig.copy(
                    id = UUID.randomUUID().toString(),
                    label = "${orig.label}+",
                    xFraction = (orig.xFraction + 0.05f).coerceAtMost(0.9f),
                    yFraction = (orig.yFraction + 0.05f).coerceAtMost(0.9f),
                    isDPad = false
                )
                val currentButtons = activeProfile.buttons.toMutableList()
                currentButtons.add(duplicate)
                scope.launch {
                    profileRepository.updateActiveProfileButtons(currentButtons)
                }
                selectedButtonForEdit = null
            },
            onDelete = { toDelete ->
                val currentButtons = activeProfile.buttons.toMutableList()
                currentButtons.removeAll { it.id == toDelete.id }
                scope.launch {
                    profileRepository.updateActiveProfileButtons(currentButtons)
                }
                selectedButtonForEdit = null
            },
            onDismiss = { selectedButtonForEdit = null }
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isEditMode) {
                    Modifier.background(Color.Black.copy(alpha = 0.45f))
                } else {
                    Modifier // completely transparent background
                }
            )
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // Virtual buttons positioned based on fraction
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
                    .then(
                        if (isEditMode) {
                            Modifier.clickable {
                                selectedButtonForEdit = button
                            }
                        } else Modifier
                    )
            ) {
                if (button.isJoystick) {
                    com.example.ui.components.PixelJoystick(
                        config = button.copy(opacity = button.opacity * activeProfile.globalOpacity),
                        isEditMode = isEditMode,
                        onPositionChanged = { dx, dy ->
                            val newXFrac = ((posX + dx + buttonWidthPx / 2f) / screenWidthPx).coerceIn(0.05f, 0.95f)
                            val newYFrac = ((posY + dy + buttonHeightPx / 2f) / screenHeightPx).coerceIn(0.05f, 0.95f)
                            val updatedList = activeProfile.buttons.map {
                                if (it.id == button.id) it.copy(xFraction = newXFrac, yFraction = newYFrac) else it
                            }
                            scope.launch { profileRepository.updateActiveProfileButtons(updatedList) }
                        }
                    )
                } else if (button.isDPad) {
                    PixelDPad(
                        config = button.copy(opacity = button.opacity * activeProfile.globalOpacity),
                        isEditMode = isEditMode,
                        onPositionChanged = { dx, dy ->
                            val newXFrac = ((posX + dx + buttonWidthPx / 2f) / screenWidthPx).coerceIn(0.05f, 0.95f)
                            val newYFrac = ((posY + dy + buttonHeightPx / 2f) / screenHeightPx).coerceIn(0.05f, 0.95f)
                            val updatedList = activeProfile.buttons.map {
                                if (it.id == button.id) it.copy(xFraction = newXFrac, yFraction = newYFrac) else it
                            }
                            scope.launch { profileRepository.updateActiveProfileButtons(updatedList) }
                        }
                    )
                } else {
                    PixelVirtualButton(
                        config = button.copy(opacity = button.opacity * activeProfile.globalOpacity),
                        isEditMode = isEditMode,
                        onButtonSelected = { selectedButtonForEdit = it },
                        onPositionChanged = { dx, dy ->
                            val newXFrac = ((posX + dx + buttonWidthPx / 2f) / screenWidthPx).coerceIn(0.05f, 0.95f)
                            val newYFrac = ((posY + dy + buttonHeightPx / 2f) / screenHeightPx).coerceIn(0.05f, 0.95f)
                            val updatedList = activeProfile.buttons.map {
                                if (it.id == button.id) it.copy(xFraction = newXFrac, yFraction = newYFrac) else it
                            }
                            scope.launch { profileRepository.updateActiveProfileButtons(updatedList) }
                        }
                    )
                }

                // If in Edit Mode, show gear icon overlay on button
                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF))
                            .clickable { selectedButtonForEdit = button },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Настроить кнопку",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Top Toolbar in Edit Mode
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF16161E).copy(alpha = 0.95f))
                        .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "РЕЖИМ РЕДАКТОРА",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Add Button
                    Button(
                        onClick = {
                            val newBtn = VirtualButtonConfig(
                                id = UUID.randomUUID().toString(),
                                label = "B",
                                keyId = "Z",
                                xFraction = 0.5f,
                                yFraction = 0.5f,
                                widthDp = 64,
                                heightDp = 64,
                                shape = ButtonShape.CIRCLE,
                                style = ButtonStyle.PIXEL,
                                opacity = 0.85f,
                                isDPad = false
                            )
                            val currentButtons = activeProfile.buttons.toMutableList()
                            currentButtons.add(newBtn)
                            scope.launch {
                                profileRepository.updateActiveProfileButtons(currentButtons)
                            }
                            selectedButtonForEdit = newBtn
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22222E)),
                        modifier = Modifier.testTag("add_custom_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Кнопка", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Done / Save
                    Button(
                        onClick = {
                            settingsRepository.setEditMode(false)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4AE290)),
                        modifier = Modifier.testTag("exit_edit_mode")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Готово", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        } else {
            // Discreet floating toggle button (in top-right corner) to easily open Edit mode or hide
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 28.dp, end = 12.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Row(
                    modifier = Modifier
                        .alpha(0.5f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { settingsRepository.setEditMode(true) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
