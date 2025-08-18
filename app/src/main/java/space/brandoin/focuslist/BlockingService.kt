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
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.serialization.json.Json
import space.brandoin.focuslist.receivers.BreakReceiver
import space.brandoin.focuslist.data.GlobalJsonStore
import space.brandoin.focuslist.screens.BlockedScreen
import space.brandoin.focuslist.ui.theme.FocusListTheme
import kotlin.random.Random

// https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo#packageNames
// https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#retrieving-window-content
// https://developer.android.com/reference/android/accessibilityservice/AccessibilityService

const val SYSTEM_UI = "com.android.systemui"
const val NEXUS_LAUNCHER = "com.google.android.apps.nexuslauncher"
const val FOCUS_LIST = "space.brandoin.focuslist"

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
    }

    override fun onDestroy() {
        super.onDestroy()
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    // TODO: add more launchers and test on samsung
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!serviceHasStarted || !showBlockOverlay || overlayView == null || event == null) return
        // prevent notification from removing block screen
        if (event.packageName == SYSTEM_UI && event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            // Notification shade
            if (event.packageName == SYSTEM_UI) {
                try {
                    if (overlayView != null && overlayInWindow) {
                        windowManager.removeView(overlayView)
                        overlayInWindow = false
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
                windowManager.addView(overlayView, getLayoutParams())
                overlayInWindow = true
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
                val extra = intent.getStringExtra("blocked_apps_json_string_extra") ?: ""
                blockedAppsExtra = Json.decodeFromString(extra)
                startBlocking()
            }

            Actions.STOP_BLOCKING.toString() -> stopBlocking()
            Actions.STOP_SERVICE.toString() -> {
                serviceHasStarted = false
                stopSelf()
            }

            Actions.UPDATE_BLOCKED_APP_LIST.toString() -> {
                val extra = intent.getStringExtra("updated_blocked_apps_json_string_extra") ?: ""
                blockedAppsExtra = Json.decodeFromString(extra)
            }

            Actions.OPEN_APP.toString() -> {
                startBlocking()
            }

            Actions.BREAK_IS_FINISHED.toString() -> {
                val openIntent = Intent(this, MainActivity::class.java).apply {
                    this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pending = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)
                val notification = NotificationCompat.Builder(this, "focus_list_break_over_channel")
                    .setSmallIcon(R.drawable.category_search_google_font)
                    .setContentTitle("Break is Over!")
                    .setContentText("Your 15 minute break is over and your apps are now blocked again.")
                    .setContentIntent(pending)
                    .setAutoCancel(true)
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    nm.notify(Random.nextInt(), notification.build())
                }
                startBlocking()
                showBlockScreen()
            }

            Actions.REQUEST_BREAK.toString() -> {
                stopBlocking()
                val alarm = getSystemService(ALARM_SERVICE) as AlarmManager
//                            val breakTime = 60_000 * 15L // 15 minutes
                val breakTime = 10000L
                val intent = Intent(this, BreakReceiver::class.java)
                alarm.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + breakTime,
                    PendingIntent.getBroadcast(
                        this,
                        1,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
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
            val requestBreakIntent = Intent(this, MainActivity::class.java).apply {
                action = Actions.REQUEST_BREAK.toString()
                putExtra(EXTRA_NOTIFICATION_ID, 0)
            }
            val requestBreakPendingIntent = PendingIntent.getActivity(
                this, 0, requestBreakIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(this, "focus_list_blocking_channel")
                .setSmallIcon(R.drawable.category_search_google_font)
                .setContentTitle("App Blocking is Active")
                .setContentText("To hide/stop showing this notification, turn off or silence notifications in your app settings.")
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.sweep_google_font, "Request Break", requestBreakPendingIntent)
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

    fun showBlockScreen() {
        try {
            windowManager.addView(overlayView, getLayoutParams())
        } catch (_: Exception) {}
        overlayInWindow = true
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
        BREAK_IS_FINISHED
    }
}