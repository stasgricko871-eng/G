package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class RetroPadApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "RetroPad Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомление активного сервиса оверлея игрового контроллера"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "retropad_overlay_channel"
        const val NOTIFICATION_ID = 1001

        lateinit var instance: RetroPadApp
            private set
    }
}
