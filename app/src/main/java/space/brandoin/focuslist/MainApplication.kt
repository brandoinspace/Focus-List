package space.brandoin.focuslist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "blocking_channel",
            "Begin Blocking Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}