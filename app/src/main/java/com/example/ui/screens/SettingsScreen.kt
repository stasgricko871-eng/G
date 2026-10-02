package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ProfileRepository
import com.example.data.repository.SettingsRepository
import com.example.input.InputBus
import com.example.util.PermissionHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val hapticFeedback by settingsRepository.hapticFeedbackFlow.collectAsState(initial = true)
    val volumeToggle by settingsRepository.volumeToggleFlow.collectAsState(initial = true)
    val activeProfile by profileRepository.activeProfileFlow.collectAsState()

    // Sync input bus haptic feedback setting
    InputBus.hapticFeedbackEnabled = hapticFeedback

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    text = "НАСТРОЙКИ",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Параметры, вибрация и безопасность",
                    color = Color(0xFF4AE290),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gameplay Settings Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color.White, RoundedCornerShape(4.dp))
                .background(Color(0xFF14141E))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "ОТКЛИК И УПРАВЛЕНИЕ",
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Haptic Feedback Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = Color(0xFF4AE290), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Тактильная отдача (Вибрация)", color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            Text("Виброотклик при нажатии на кнопки", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = { scope.launch { settingsRepository.setHapticFeedback(it) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF4AE290),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF22222E)
                        )
                    )
                }

                // Volume Up Toggle Feature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF4AE290), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Переключение по Volume Up", color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            Text("Скрыть/показать оверлей физической клавишей", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Switch(
                        checked = volumeToggle,
                        onCheckedChange = { scope.launch { settingsRepository.setVolumeToggle(it) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF4AE290),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF22222E)
                        )
                    )
                }

                // Global Opacity Slider for Active Profile
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Общая прозрачность кнопок", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Text("${(activeProfile.globalOpacity * 100).toInt()}%", color = Color(0xFF4AE290), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    Slider(
                        value = activeProfile.globalOpacity,
                        onValueChange = { scope.launch { profileRepository.updateActiveProfileOpacity(it) } },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4AE290),
                            activeTrackColor = Color(0xFF4AE290),
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System Permissions & Security Info Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                .background(Color(0xFF0D141C))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "РАЗРЕШЕНИЯ И БЕЗОПАСНОСТЬ ANDROID",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "1. «Display over other apps» (SYSTEM_ALERT_WINDOW)\n" +
                            "Необходимо для рисования прозрачного оверлея поверх других полноэкранных игр и браузеров.\n\n" +
                            "2. «AccessibilityService» (Спец. возможности)\n" +
                            "Официальный API Android с флагом flagRequestFilterKeyEvents. Позволяет глобально обнаруживать нажатие аппаратной кнопки Volume Up во время игры и возвращать true для подавления стандартного диалога громкости.\n\n" +
                            "3. Модель безопасности ввода Android:\n" +
                            "Android запрещает сторонним приложениям отправлять произвольные KeyEvent в другие несвязанные приложения без Root или системного статуса IME (клавиатуры). Поэтому контроллер рассчитан на игры с поддержкой клавиатурного ввода, WebView/HTML-игры, порты и встроенную игровую среду приложения.",
                    color = Color(0xFFD0D8E0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { PermissionHelper.openOverlaySettings(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222838))
                ) {
                    Text("Настройки оверлея Android", color = Color(0xFF00E5FF), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }

                Button(
                    onClick = { PermissionHelper.openAccessibilitySettings(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222838))
                ) {
                    Text("Настройки Спец. возможностей", color = Color(0xFF4AE290), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
