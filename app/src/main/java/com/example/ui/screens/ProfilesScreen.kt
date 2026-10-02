package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ControllerProfile
import com.example.data.repository.ProfileRepository
import com.example.ui.components.ImportExportJsonDialog
import com.example.ui.components.ProfileNameDialog
import kotlinx.coroutines.launch

@Composable
fun ProfilesScreen(
    profileRepository: ProfileRepository,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val allProfiles by profileRepository.allProfilesFlow.collectAsState(initial = emptyList())
    val activeProfile by profileRepository.activeProfileFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var profileToRename by remember { mutableStateOf<ControllerProfile?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var jsonExportProfile by remember { mutableStateOf<ControllerProfile?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        ProfileNameDialog(
            initialName = "Мой профиль",
            title = "СОЗДАТЬ ПРОФИЛЬ",
            onConfirm = { name ->
                scope.launch {
                    val created = profileRepository.createProfile(name)
                    snackbarHostState.showSnackbar("Профиль «${created.name}» создан!")
                }
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (profileToRename != null) {
        ProfileNameDialog(
            initialName = profileToRename!!.name,
            title = "ПЕРЕИМЕНОВАТЬ ПРОФИЛЬ",
            onConfirm = { newName ->
                scope.launch {
                    profileRepository.renameProfile(profileToRename!!.id, newName)
                    snackbarHostState.showSnackbar("Профиль переименован")
                }
                profileToRename = null
            },
            onDismiss = { profileToRename = null }
        )
    }

    if (jsonExportProfile != null) {
        ImportExportJsonDialog(
            isImport = false,
            initialJson = jsonExportProfile!!.toJsonString(),
            onDismiss = { jsonExportProfile = null }
        )
    }

    if (showImportDialog) {
        ImportExportJsonDialog(
            isImport = true,
            onImport = { jsonStr ->
                scope.launch {
                    val res = profileRepository.importProfileFromJson(jsonStr)
                    if (res.isSuccess) {
                        snackbarHostState.showSnackbar("Профиль «${res.getOrNull()?.name}» успешно импортирован!")
                    } else {
                        snackbarHostState.showSnackbar("Ошибка импорта: проверьте формат JSON")
                    }
                }
            },
            onDismiss = { showImportDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0F))
                .padding(16.dp)
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
                        text = "ПРОФИЛИ УПРАВЛЕНИЯ",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Выбор, экспорт и настройка раскладок",
                        color = Color(0xFF4AE290),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row: Create & Import
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("create_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4AE290))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Создать", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }

                OutlinedButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("import_profile_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Импорт JSON", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Profiles List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allProfiles) { profile ->
                    val isActive = profile.id == activeProfile.id

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isActive) Color(0xFF14241C) else Color(0xFF14141E))
                            .border(
                                2.dp,
                                if (isActive) Color(0xFF4AE290) else Color(0xFF333344),
                                RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                scope.launch { profileRepository.setActiveProfile(profile.id) }
                            }
                            .padding(12.dp)
                            .testTag("profile_item_${profile.name.lowercase().replace(" ", "_")}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isActive) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Активен",
                                            tint = Color(0xFF4AE290),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = profile.name,
                                        color = if (isActive) Color(0xFF4AE290) else Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF4AE290))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "АКТИВЕН",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Кнопок: ${profile.buttons.size} • Прозрачность: ${(profile.globalOpacity * 100).toInt()}%",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Profile Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rename
                                IconButton(
                                    onClick = { profileToRename = profile },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Переименовать", tint = Color.White, modifier = Modifier.size(16.dp))
                                }

                                // Duplicate
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            profileRepository.duplicateProfile(profile.id)
                                            snackbarHostState.showSnackbar("Профиль скопирован!")
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Дублировать", tint = Color.White, modifier = Modifier.size(16.dp))
                                }

                                // Export JSON
                                IconButton(
                                    onClick = { jsonExportProfile = profile },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Экспорт JSON", tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                }

                                // Delete (if more than 1 profile)
                                if (allProfiles.size > 1) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                val deleted = profileRepository.deleteProfile(profile.id)
                                                if (deleted) {
                                                    snackbarHostState.showSnackbar("Профиль удалён")
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
