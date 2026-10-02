package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.RetroPadApp
import com.example.data.repository.ProfileRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OverlayService : Service() {

    private lateinit var profileRepository: ProfileRepository
    private lateinit var settingsRepository: SettingsRepository
    private var windowManagerHelper: OverlayWindowManager? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        instance = this
        profileRepository = ProfileRepository(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)

        windowManagerHelper = OverlayWindowManager(
            context = this,
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            onCloseRequest = { stopSelf() }
        )

        settingsRepository.setOverlayRunning(true)

        // Observe visibility, edit mode and profile changes
        serviceScope.launch {
            kotlinx.coroutines.flow.combine(
                settingsRepository.isOverlayVisible,
                settingsRepository.isEditMode,
                profileRepository.activeProfileFlow
            ) { visible, isEdit, profile ->
                Triple(visible, isEdit, profile)
            }.collectLatest { (visible, isEdit, profile) ->
                windowManagerHelper?.updateLayout(visible, isEdit, profile)
                updateNotification()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_VISIBILITY -> {
                settingsRepository.toggleOverlayVisible()
            }
            ACTION_TOGGLE_EDIT_MODE -> {
                settingsRepository.toggleEditMode()
            }
            ACTION_START -> {
                settingsRepository.setOverlayVisible(true)
            }
        }

        startForegroundWithNotification()
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                RetroPadApp.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(RetroPadApp.NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        manager?.notify(RetroPadApp.NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        val isVisible = settingsRepository.isOverlayVisible.value
        val isEdit = settingsRepository.isEditMode.value

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleVisIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_TOGGLE_VISIBILITY
        }
        val toggleVisPendingIntent = PendingIntent.getService(
            this, 1, toggleVisIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleEditIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_TOGGLE_EDIT_MODE
        }
        val toggleEditPendingIntent = PendingIntent.getService(
            this, 2, toggleEditIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 3, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (isVisible) "Оверлей показан (Volume Up скрывает)" else "Оверлей скрыт (Volume Up показывает)"

        return NotificationCompat.Builder(this, RetroPadApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("RetroPad: Оверлей активен")
            .setContentText(statusText)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                0,
                if (isVisible) "Скрыть" else "Показать",
                toggleVisPendingIntent
            )
            .addAction(
                0,
                if (isEdit) "Выйти из ред." else "Редактор",
                toggleEditPendingIntent
            )
            .addAction(
                0,
                "Остановить",
                stopPendingIntent
            )
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        settingsRepository.setOverlayRunning(false)
        windowManagerHelper?.destroy()
        windowManagerHelper = null
        if (instance == this) {
            instance = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.retropad.ACTION_START"
        const val ACTION_STOP = "com.example.retropad.ACTION_STOP"
        const val ACTION_TOGGLE_VISIBILITY = "com.example.retropad.ACTION_TOGGLE_VISIBILITY"
        const val ACTION_TOGGLE_EDIT_MODE = "com.example.retropad.ACTION_TOGGLE_EDIT_MODE"

        var instance: OverlayService? = null
            private set

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun toggleVisibility(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_TOGGLE_VISIBILITY
            }
            if (instance != null) {
                instance?.settingsRepository?.toggleOverlayVisible()
            } else {
                start(context)
            }
        }

        fun toggleEditMode(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_TOGGLE_EDIT_MODE
            }
            context.startService(intent)
        }
    }
}
