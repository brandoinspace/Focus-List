package space.brandoin.focuslist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import space.brandoin.focuslist.data.GlobalDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "global"
)

class MainApplication: Application() {
    lateinit var globalDataStore: GlobalDataStore
    override fun onCreate() {
        super.onCreate()
        globalDataStore = GlobalDataStore(dataStore)
        val channel = NotificationChannel(
            "blocking_channel",
            "Begin Blocking Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}