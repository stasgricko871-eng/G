package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.data.repository.ProfileRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TestControllerScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    MAIN, TEST, PROFILES, SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var profileRepository: ProfileRepository
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        profileRepository = ProfileRepository(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                // Request notification permission on Android 13+ if needed for Foreground Service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { _ -> }

                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0A0F))
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                    ) {
                        when (currentScreen) {
                            AppScreen.MAIN -> {
                                MainDashboardScreen(
                                    profileRepository = profileRepository,
                                    settingsRepository = settingsRepository,
                                    onNavigateToTest = { currentScreen = AppScreen.TEST },
                                    onNavigateToProfiles = { currentScreen = AppScreen.PROFILES },
                                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
                                )
                            }
                            AppScreen.TEST -> {
                                TestControllerScreen(
                                    profileRepository = profileRepository,
                                    onBack = { currentScreen = AppScreen.MAIN }
                                )
                            }
                            AppScreen.PROFILES -> {
                                ProfilesScreen(
                                    profileRepository = profileRepository,
                                    onBack = { currentScreen = AppScreen.MAIN }
                                )
                            }
                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    profileRepository = profileRepository,
                                    settingsRepository = settingsRepository,
                                    onBack = { currentScreen = AppScreen.MAIN }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
