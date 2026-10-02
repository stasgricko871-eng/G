package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ProfileRepository
import com.example.data.repository.SettingsRepository
import com.example.service.OverlayService
import com.example.util.PermissionHelper

@Composable
fun MainDashboardScreen(
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    onNavigateToTest: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val activeProfile by profileRepository.activeProfileFlow.collectAsState()
    val isOverlayRunning by settingsRepository.isOverlayRunning.collectAsState()
    val isEditMode by settingsRepository.isEditMode.collectAsState()

    var hasOverlayPermission by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(PermissionHelper.isAccessibilityServiceEnabled(context)) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    // Periodically refresh permission states when returning from settings
    LaunchedEffect(Unit) {
        hasOverlayPermission = PermissionHelper.hasOverlayPermission(context)
        hasAccessibilityPermission = PermissionHelper.isAccessibilityServiceEnabled(context)
    }

    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            containerColor = Color(0xFF14141E),
            title = {
                Text(
                    text = "РАЗРЕШЕНИЕ ОВЕРЛЕЯ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Text(
                    text = "Для отображения игрового контроллера поверх других игр и приложений требуется системное разрешение «Отображение поверх других приложений» (SYSTEM_ALERT_WINDOW).\n\nНажмите кнопку ниже, чтобы предоставить разрешение в настройках Android.",
                    color = Color(0xFFDDDDDD),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverlayPermissionDialog = false
                        PermissionHelper.openOverlaySettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4AE290))
                ) {
                    Text("Разрешить", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showOverlayPermissionDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Отмена", fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Retro Title & Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.White, RoundedCornerShape(4.dp))
                .background(Color(0xFF14141E))
                .padding(vertical = 16.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "⚔ RETROPAD ⚔",
                    color = Color(0xFF4AE290),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Экранный ретро-геймпад поверх игр",
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.White, RoundedCornerShape(4.dp))
                .background(Color(0xFF12121A))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "СТАТУС СИСТЕМЫ",
                    color = Color(0xFF00E5FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Overlay Permission
                StatusRow(
                    label = "Разрешение поверх приложений:",
                    statusText = if (hasOverlayPermission) "РАЗРЕШЕНО ✓" else "НЕ ВЫДАНО ✕",
                    statusColor = if (hasOverlayPermission) Color(0xFF4AE290) else Color(0xFFFF5252),
                    actionText = if (!hasOverlayPermission) "ВЫДАТЬ" else null,
                    onAction = { PermissionHelper.openOverlaySettings(context) }
                )

                // Accessibility Permission
                StatusRow(
                    label = "Перехват Volume Up:",
                    statusText = if (hasAccessibilityPermission) "АКТИВНО ✓" else "ВЫКЛЮЧЕНО ✕",
                    statusColor = if (hasAccessibilityPermission) Color(0xFF4AE290) else Color(0xFFFFCC00),
                    actionText = if (!hasAccessibilityPermission) "ВКЛЮЧИТЬ" else null,
                    onAction = { PermissionHelper.openAccessibilitySettings(context) }
                )

                // Service Status
                StatusRow(
                    label = "Сервис оверлея:",
                    statusText = if (isOverlayRunning) "РАБОТАЕТ (ON)" else "ВЫКЛЮЧЕН (OFF)",
                    statusColor = if (isOverlayRunning) Color(0xFF4AE290) else Color.Gray,
                    actionText = null,
                    onAction = null
                )

                // Active Profile
                StatusRow(
                    label = "Текущий профиль:",
                    statusText = activeProfile.name,
                    statusColor = Color(0xFF00E5FF),
                    actionText = "СМЕНИТЬ",
                    onAction = onNavigateToProfiles
                )

                // Edit Mode Status
                StatusRow(
                    label = "Режим:",
                    statusText = if (isEditMode) "РЕДАКТИРОВАНИЕ (ПЕРЕМЕЩЕНИЕ)" else "ИГРОВОЙ (ПРОЗРАЧНЫЙ ДЛЯ ИГРЫ)",
                    statusColor = if (isEditMode) Color(0xFF00E5FF) else Color(0xFF4AE290),
                    actionText = if (isEditMode) "ЗАВЕРШИТЬ" else "РЕДАКТОР",
                    onAction = { settingsRepository.toggleEditMode() }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Volume Up Info Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF4AE290), RoundedCornerShape(4.dp))
                .background(Color(0xFF0E1A14))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = Color(0xFF4AE290),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Физическая клавиша Volume Up скрывает и показывает управление без изменения громкости мультимедиа!",
                    color = Color(0xFFB9F6CA),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Primary Launch / Stop Controls
        if (!isOverlayRunning) {
            RetroMenuButton(
                title = "ЗАПУСТИТЬ УПРАВЛЕНИЕ",
                subtitle = "Включить оверлей поверх игр",
                icon = Icons.Default.PlayArrow,
                containerColor = Color(0xFF4AE290),
                contentColor = Color.Black,
                testTag = "start_overlay_button",
                onClick = {
                    if (!PermissionHelper.hasOverlayPermission(context)) {
                        showOverlayPermissionDialog = true
                    } else {
                        OverlayService.start(context)
                    }
                }
            )
        } else {
            RetroMenuButton(
                title = "ОСТАНОВИТЬ УПРАВЛЕНИЕ",
                subtitle = "Отключить оверлей",
                icon = Icons.Default.Stop,
                containerColor = Color(0xFFFF5252),
                contentColor = Color.White,
                testTag = "stop_overlay_button",
                onClick = {
                    OverlayService.stop(context)
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Edit Overlay Button (Switches between Edit and Save/Done)
        if (isEditMode) {
            RetroMenuButton(
                title = "ЗАВЕРШИТЬ РЕДАКТИРОВАНИЕ ✓",
                subtitle = "Сохранить позиции и вернуться в игру",
                icon = Icons.Default.CheckCircle,
                containerColor = Color(0xFF4AE290),
                contentColor = Color.Black,
                borderColor = Color.White,
                testTag = "save_edit_button",
                onClick = {
                    settingsRepository.setEditMode(false)
                }
            )
        } else {
            RetroMenuButton(
                title = "РЕДАКТИРОВАТЬ УПРАВЛЕНИЕ",
                subtitle = "Перетаскивание, размер, клавиши",
                icon = Icons.Default.Edit,
                containerColor = Color(0xFF1E1E2A),
                contentColor = Color.White,
                borderColor = Color(0xFF00E5FF),
                testTag = "edit_overlay_button",
                onClick = {
                    if (!PermissionHelper.hasOverlayPermission(context)) {
                        showOverlayPermissionDialog = true
                    } else {
                        settingsRepository.setEditMode(true)
                        OverlayService.start(context)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Test Controller Area
        RetroMenuButton(
            title = "ТЕСТ УПРАВЛЕНИЯ",
            subtitle = "Игровая арена, мультач и дебаг",
            icon = Icons.Default.Gamepad,
            containerColor = Color(0xFF1E1E2A),
            contentColor = Color.White,
            testTag = "test_controller_button",
            onClick = onNavigateToTest
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Profiles Screen
        RetroMenuButton(
            title = "ПРОФИЛИ",
            subtitle = "Retro RPG, HTML Game, Импорт/Экспорт",
            icon = Icons.Default.Style,
            containerColor = Color(0xFF1E1E2A),
            contentColor = Color.White,
            testTag = "profiles_button",
            onClick = onNavigateToProfiles
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Settings Screen
        RetroMenuButton(
            title = "НАСТРОЙКИ",
            subtitle = "Вибрация, прозрачность, разрешения",
            icon = Icons.Default.Settings,
            containerColor = Color(0xFF1E1E2A),
            contentColor = Color.White,
            testTag = "settings_button",
            onClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun StatusRow(
    label: String,
    statusText: String,
    statusColor: Color,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (actionText != null && onAction != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF262636))
                    .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                    .clickable { onAction() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = actionText,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun RetroMenuButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color = Color.White,
    testTag: String = "",
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .border(2.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = contentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = subtitle,
                    color = if (contentColor == Color.Black) Color(0xFF222222) else Color(0xFFAAAAAA),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
