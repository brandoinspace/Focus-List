package space.brandoin.focuslist

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.PendingIntent
import android.content.Intent
import android.graphics.PixelFormat
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.ui.platform.ComposeView
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
import space.brandoin.focuslist.data.BlockedAppsJSONStore
import space.brandoin.focuslist.screens.BlockedScreen
import space.brandoin.focuslist.viewmodels.AppInfo

// https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo#packageNames
// https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#retrieving-window-content
// https://developer.android.com/reference/android/accessibilityservice/AccessibilityService

// https://www.techyourchance.com/jetpack-compose-inside-android-service/
class BlockingService : AccessibilityService(), LifecycleOwner, SavedStateRegistryOwner {
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    private val _lifecycleRegistry = LifecycleRegistry(this)
    private val _savedStateRegistryController: SavedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = _savedStateRegistryController.savedStateRegistry
    override val lifecycle: Lifecycle
        get() = _lifecycleRegistry

    private var overlayInWindow = false
    private var showBlockOverlay = false
    private var serviceHasStarted = false

    private var blockedAppsExtra = emptyList<AppInfo>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = this.serviceInfo
        info.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
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

    // TODO: add more launchers
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!serviceHasStarted || !showBlockOverlay || overlayView == null || event == null) return
        // prevent notification from removing block screen
        if (event.packageName == "com.android.systemui" && event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return
        val packageNames = blockedAppsExtra.map { appInfo -> appInfo.packageName }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // systemui = notification shade
            if (event.packageName == "com.android.systemui" || (event.packageName == "com.google.android.apps.nexuslauncher" && event.isFullScreen)) {
                try {
                    windowManager.removeView(overlayView)
                    overlayInWindow = false
                } catch (e: Exception) {
                    Log.d("Focus List CATCH ->", e.message ?: "")
                }
            } else if (packageNames.contains(event.packageName) && event.isFullScreen && !overlayInWindow) {
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

    // TODO: pass blocked app list between service and view
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Actions.START_BLOCKING.toString() -> {
                val extra = intent.getStringExtra("blocked_apps_json_string_extra") ?: ""
                blockedAppsExtra = BlockedAppsJSONStore.decodeToBlockedAppsList(extra)
                startBlocking()
            }
            Actions.STOP_BLOCKING.toString() -> stopBlocking()
            Actions.STOP_SERVICE.toString() -> {
                serviceHasStarted = false
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startBlocking() {
        if (!serviceHasStarted) {
            val openIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)
            val requestBreakIntent = Intent(this, MainActivity::class.java).apply {
                action = Actions.REQUEST_BREAK.toString()
                putExtra(EXTRA_NOTIFICATION_ID, 0)
            }
            val requestBreakPendingIntent = PendingIntent.getActivity(this, 0, requestBreakIntent,
                PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(this, "blocking_channel")
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
            PixelFormat.TRANSLUCENT)
    }

    private fun blockScreen(): ComposeView {
        return ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@BlockingService)
            setViewTreeSavedStateRegistryOwner(this@BlockingService)
            setContent {
                BlockedScreen()
            }
        }
    }

    private fun stopBlocking() {
        if (overlayView != null && overlayInWindow) {
            windowManager.removeView(overlayView)
        }
        overlayInWindow = false
        showBlockOverlay = false
    }

    // TODO: Apps do not become re-blocked if a task is re-marked as incomplete
    enum class Actions {
        START_BLOCKING,
        STOP_BLOCKING,
        REQUEST_BREAK,
        STOP_SERVICE,
    }
}