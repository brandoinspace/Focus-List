package space.brandoin.focuslist

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import me.zhanghai.compose.preference.LocalPreferenceFlow
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import space.brandoin.focuslist.data.GlobalJsonStore
import space.brandoin.focuslist.receivers.BreakReceiver
import space.brandoin.focuslist.receivers.CooldownReceiver
import space.brandoin.focuslist.screens.BREAK_COOLDOWN
import space.brandoin.focuslist.screens.BREAK_COOLDOWN_DEFAULT
import space.brandoin.focuslist.screens.BREAK_TIME
import space.brandoin.focuslist.screens.BREAK_TIME_DEFAULT
import space.brandoin.focuslist.screens.BlockedScreen
import space.brandoin.focuslist.ui.theme.FocusListTheme
import kotlin.random.Random

// https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo#packageNames
// https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#retrieving-window-content
// https://developer.android.com/reference/android/accessibilityservice/AccessibilityService

const val SYSTEM_UI = "com.android.systemui"
const val NEXUS_LAUNCHER = "com.google.android.apps.nexuslauncher"
const val FOCUS_LIST = "space.brandoin.focuslist"

var IS_BLOCKING_SERVICE_RUNNING by mutableStateOf(false)
var BREAK_ALARM_INTENT: PendingIntent? by mutableStateOf(null)
var COOLDOWN_ALARM_INTENT: PendingIntent? by mutableStateOf(null)
var ACCESSIBILITY_ENABLED by mutableStateOf(false)

// https://www.techyourchance.com/jetpack-compose-inside-android-service/
class BlockingService : AccessibilityService(), LifecycleOwner, SavedStateRegistryOwner {
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    private val _lifecycleRegistry = LifecycleRegistry(this)
    private val _savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = _savedStateRegistryController.savedStateRegistry
    override val lifecycle: Lifecycle
        get() = _lifecycleRegistry

    // TODO: better state managing
    private var overlayInWindow = false
    private var showBlockOverlay = false
    private var serviceHasStarted = false

    private var onABreak = false
    private var showBlockScreenAfterBreak = false

    private var blockedAppsExtra = emptyList<String>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = this.serviceInfo
        info.apply {
            eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED or AccessibilityEvent.TYPE_VIEW_SCROLLED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        this.serviceInfo = info
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        _savedStateRegistryController.performAttach()
        _savedStateRegistryController.performRestore(null)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        IS_BLOCKING_SERVICE_RUNNING = true
    }

    override fun onDestroy() {
        super.onDestroy()
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        IS_BLOCKING_SERVICE_RUNNING = false
    }

    // TODO: add more launchers and test on samsung
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!serviceHasStarted || !showBlockOverlay || overlayView == null) {
            if (!onABreak) {
                return
            }
        }
        // prevent notification from removing block screen
        if (event.packageName == SYSTEM_UI && event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED || event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Notification shade
            if (event.packageName == SYSTEM_UI) {
                try {
                    if (!onABreak) {
                        if (overlayView != null && overlayInWindow) {
                            windowManager.removeView(overlayView)
                            overlayInWindow = false
                        }
                    } else {
                        showBlockScreenAfterBreak = false
                    }
                } catch (e: Exception) {
                    Log.d("Focus List CATCH ->", e.message ?: "")
                }
                return
            }
        }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if ((event.packageName == NEXUS_LAUNCHER && event.isFullScreen)) {
                try {
                    if (overlayView != null && overlayInWindow) {
                        windowManager.removeView(overlayView)
                        overlayInWindow = false
                    }
                } catch (e: Exception) {
                    Log.d("Focus List CATCH ->", e.message ?: "")
                }
            } else if ((blockedAppsExtra.contains(event.packageName) || (GlobalJsonStore.readShouldBlockAllJSON() && event.packageName != FOCUS_LIST)) && event.isFullScreen && !overlayInWindow) {
                if (!onABreak) {
                    showBlockScreen()
                } else {
                    showBlockScreenAfterBreak = true
                }
            }
        }
    }

    // TODO: give this actual functionality
    override fun onInterrupt() {
        stopBlocking()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopBlocking()
        serviceHasStarted = false
        stopSelf()
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START_BLOCKING.toString() -> {
                blockedAppsExtra = GlobalJsonStore.readBlockedAppsJSON().map { appInfo ->
                    appInfo.packageName
                }
                startBlocking()
            }

            Actions.STOP_BLOCKING.toString() -> stopBlocking()
            Actions.STOP_SERVICE.toString() -> {
                serviceHasStarted = false
                stopSelf()
            }

            Actions.UPDATE_BLOCKED_APP_LIST.toString() -> {
                blockedAppsExtra = GlobalJsonStore.readBlockedAppsJSON().map { appInfo ->
                    appInfo.packageName
                }
            }

            Actions.OPEN_APP.toString() -> {
                startBlocking()
            }

            Actions.BREAK_IS_FINISHED.toString(), Actions.CANCEL_BREAK.toString() -> {
                if (BREAK_ALARM_INTENT != null) {
                    Log.d("Focus List Break", "Break finished or cancelled.")
                    val cancelled = intent.action == Actions.CANCEL_BREAK.toString()
                    if (cancelled) {
                        val alarm = getSystemService(ALARM_SERVICE) as AlarmManager
                        try {
                            alarm.cancel(BREAK_ALARM_INTENT!!)
                        } catch (e: Exception) {
                            Log.d("FOCUS LIST BREAK", e.message ?: "")
                        }
                    }
                    val openIntent = Intent(this, MainActivity::class.java).apply {
                        this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pending = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)
                    val notification = NotificationCompat.Builder(this, "focus_list_break_over_channel")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Break is Over!")
                        .setContentText("Your break is over and your apps are now blocked again.")
                        .setContentIntent(pending)
                        .setAutoCancel(true)
                    val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        nm.notify(Random.nextInt(), notification.build())
                    }
                    startBlocking()
                    if (!cancelled) {
                        if (showBlockScreenAfterBreak) {
                            showBlockScreen()
                        }
                    }
                    onABreak = false
                    BREAK_ALARM_INTENT = null

                    val cooldownTime = GlobalJsonStore.readCooldownTime()

                    if (cooldownTime != 0L) {
                        Log.d("focus list break", "cooldown extra: $cooldownTime")
                        val alarm = getSystemService(ALARM_SERVICE) as AlarmManager
                        val intent = Intent(this, CooldownReceiver::class.java)
                        COOLDOWN_ALARM_INTENT = PendingIntent.getBroadcast(
                            this,
                            2,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        // https://developer.android.com/about/versions/14/changes/schedule-exact-alarms#migration
                        if (alarm.canScheduleExactAlarms()) {
                            alarm.setExactAndAllowWhileIdle(
                                AlarmManager.ELAPSED_REALTIME,
                                SystemClock.elapsedRealtime() + cooldownTime,
                                COOLDOWN_ALARM_INTENT!!
                            )
                        } else {
                            alarm.setAndAllowWhileIdle(
                                AlarmManager.ELAPSED_REALTIME,
                                SystemClock.elapsedRealtime() + cooldownTime,
                                COOLDOWN_ALARM_INTENT!!
                            )
                        }
                        Log.d("focus list", "cooldown started")
                    }
                }
            }

            Actions.REQUEST_BREAK.toString() -> {
                stopBlocking()
                ShortcutManagerCompat.disableShortcuts(this, listOf("request_break"), R.string.break_cooldown_shortcut_disabled.toString())
                val alarm = getSystemService(ALARM_SERVICE) as AlarmManager
                val breakTime = intent.getIntExtra("break_time_minutes_extra", BREAK_TIME_DEFAULT) * 60_000L
                val cooldownTime = intent.getIntExtra("cooldown_time_minutes_extra_request", BREAK_COOLDOWN_DEFAULT) * 60_000L
                GlobalJsonStore.writeCooldownTime(cooldownTime)
                Log.d("focus list break", "extra: $breakTime")
                val intent = Intent(this, BreakReceiver::class.java)
                BREAK_ALARM_INTENT = PendingIntent.getBroadcast(
                    this,
                    1,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                if (alarm.canScheduleExactAlarms()) {
                    alarm.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + breakTime,
                        BREAK_ALARM_INTENT!!
                    )
                } else {
                    alarm.setAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + breakTime,
                        BREAK_ALARM_INTENT!!
                    )
                }
                onABreak = true
            }

            Actions.COOLDOWN_IS_FINISHED.toString() -> {
                if (COOLDOWN_ALARM_INTENT != null) {
                    val openIntent = Intent(this, MainActivity::class.java).apply {
                        this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pending = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)
                    val notification = NotificationCompat.Builder(this, "focus_list_cooldown_break_over_channel")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Break Cooldown is Over!")
                        .setContentText("Your break cooldown is over and you can take another break.")
                        .setContentIntent(pending)
                        .setAutoCancel(true)
                    val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        nm.notify(Random.nextInt(), notification.build())
                    }
                    COOLDOWN_ALARM_INTENT = null
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startBlocking() {
        if (!serviceHasStarted) {
            val openIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent =
                PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(this, "focus_list_blocking_channel")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("App Blocking is Active")
                .setContentText("To hide/stop showing this notification, turn off or silence notifications in your app settings.")
                .setContentIntent(pendingIntent)
                .build()
            startForeground(1, notification)
            serviceHasStarted = true
        }

        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        overlayView = blockScreen()
        showBlockOverlay = true
    }

    private fun getLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }

    private fun blockScreen(): ComposeView {
        return ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@BlockingService)
            setViewTreeSavedStateRegistryOwner(this@BlockingService)
            setContent {
                FocusListTheme {
                    ProvidePreferenceLocals {
                        val current = LocalPreferenceFlow.current
                        BlockedScreen(
                            {
                                startActivity(
                                    Intent(this@BlockingService, MainActivity::class.java)
                                        .also {
                                            it.action = Actions.OPEN_APP.toString()
                                        }.apply {
                                            flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        })
                                stopBlocking()
                            },
                            {
                                Intent(this@BlockingService, BlockingService::class.java)
                                    .putExtra("break_time_minutes_extra", current.value[BREAK_TIME] ?: BREAK_TIME_DEFAULT)
                                    .putExtra("cooldown_time_minutes_extra_request", current.value[BREAK_COOLDOWN] ?: BREAK_COOLDOWN_DEFAULT)
                                    .also {
                                        it.action = Actions.REQUEST_BREAK.toString()
                                        startService(it)
                                    }
                            },
                            GlobalJsonStore.readPercentageJSON()
                        )
                    }
                }
            }
        }
    }

    fun showBlockScreen() {
        try {
            overlayView = blockScreen()
            windowManager.addView(overlayView, getLayoutParams())
            overlayInWindow = true
        } catch (e: Exception) {
            Log.d("focus list error", e.message ?: "")
        }
    }

    private fun stopBlocking() {
        if (overlayView != null && overlayInWindow) {
            windowManager.removeView(overlayView)
        }
        overlayInWindow = false
        showBlockOverlay = false
    }

    enum class Actions {
        START_BLOCKING,
        STOP_BLOCKING,
        REQUEST_BREAK,
        STOP_SERVICE,
        UPDATE_BLOCKED_APP_LIST,
        OPEN_APP,
        BREAK_IS_FINISHED,
        CANCEL_BREAK,
        COOLDOWN_IS_FINISHED,
    }
}