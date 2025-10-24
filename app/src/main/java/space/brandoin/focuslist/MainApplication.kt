package space.brandoin.focuslist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import space.brandoin.focuslist.data.tasks.TasksDatabase
import space.brandoin.focuslist.data.tasks.TasksRepository

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        container = lazy {
            TasksRepository(TasksDatabase.getDatabase(this).tasksDao())
        }.value
        val breakGroup = "break_group"
        val blockingGroup = "blocking_group"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannelGroup(NotificationChannelGroup(breakGroup, "Breaks and Cooldowns"))
        notificationManager.createNotificationChannelGroup(NotificationChannelGroup(blockingGroup, "Blocking"))
        val blockingChannel = NotificationChannel(
            "focus_list_blocking_channel",
            "Begin Blocking Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            group = blockingGroup
            description = "The notification that is sent to let you know that the Blocking service has begun."
        }
        val breakChannel = NotificationChannel(
            "focus_list_break_over_channel",
            "Break is Over Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            enableVibration(true)
            group = breakGroup
            description = "The notification that is sent when your break is over and the app will be blocked again."
        }
        val cooldownBreakChannel = NotificationChannel(
            "focus_list_cooldown_break_over_channel",
            "Break Cooldown in Over Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            enableVibration(true)
            group = breakGroup
            description = "The notification that is sent when your break cooldown is over and you can take another break."
        }
        notificationManager.createNotificationChannel(blockingChannel)
        notificationManager.createNotificationChannel(breakChannel)
        notificationManager.createNotificationChannel(cooldownBreakChannel)
    }

    companion object {
        lateinit var container: TasksRepository
    }
}