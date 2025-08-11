package space.brandoin.focuslist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        val blockingChannel = NotificationChannel(
            "focus_list_blocking_channel",
            "Begin Blocking Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        blockingChannel.description = "The notification that is sent to let you know that the Blocking service has begun."
        val breakChannel = NotificationChannel(
            "focus_list_break_over_channel",
            "Break is Over Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            enableVibration(true)
        }
        breakChannel.description = "The notification that is sent when your break is over and the app will be blocked again."
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(blockingChannel)
        notificationManager.createNotificationChannel(breakChannel)
    }
}