package space.brandoin.focuslist

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.LocalPreferenceFlow
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import space.brandoin.focuslist.BlockingService.Actions
import space.brandoin.focuslist.data.GlobalJsonStore
import space.brandoin.focuslist.screens.AppBlockList
import space.brandoin.focuslist.screens.BREAK_TIME
import space.brandoin.focuslist.screens.BREAK_TIME_DEFAULT
import space.brandoin.focuslist.screens.BreakSettingsScreen
import space.brandoin.focuslist.screens.MainSettingsScreen
import space.brandoin.focuslist.screens.MainTodoScreen
import space.brandoin.focuslist.screens.PermissionScreen
import space.brandoin.focuslist.ui.theme.FocusListTheme


// https://www.youtube.com/watch?v=jhrfx8Uk_y0
@Serializable
object MainScreen: NavKey

@Serializable
object MainSettingsScreen: NavKey

@Serializable
object AppBlockListScreen: NavKey

@Serializable
object BreakSettingsScreen: NavKey

@Serializable
object PermissionsScreen: NavKey

// TODO: permission handling
// TODO: keyboard focus
// TODO: see if material 3 expressive works for older android versions
// TODO: put all strings into Resources
// TODO: task widget
// TODO: task shortcuts
// TODO: auto start service
// TODO: battery optimisation? (https://developer.android.com/reference/android/Manifest.permission#REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
// TODO: proper exception handling
// TODO: add service status to rerun service if not running instead of using add button
// TODO: block screen animation stops after opening a second time
// TODO: experiment with more material 3 expressive ui
// TODO: fix padding differences between screens
// https://developer.android.com/develop/ui/views/components/settings
// TODO: Break cooldown
// TODO: Break indicator on main screen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        GlobalJsonStore.filesDir = this.filesDir

        // Fixes issue that all blocking will stop when
        // pressing the "Open FocusList" button on blocked screen
        if (intent != null) {
            if (intent.action == Actions.OPEN_APP.toString()) {
                startBlocking()
            }
        }

        setContent {
            FocusListTheme {
                ProvidePreferenceLocals {
                    var backStack = rememberNavBackStack(MainScreen)
                    if (GlobalJsonStore.isFirstTime()) {
                        backStack = rememberNavBackStack(PermissionsScreen)
                    }
                    val current = LocalPreferenceFlow.current

                    NavDisplay(
                        backStack = backStack,
                        entryDecorators = listOf(
                            rememberSavedStateNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                            rememberSceneSetupNavEntryDecorator()
                        ),
                        entryProvider = { key ->
                            when(key) {
                                is PermissionsScreen -> {
                                    NavEntry(
                                        key = key
                                    ) {
                                        PermissionScreen(
                                            GlobalJsonStore.isFirstTime(),
                                            {
                                                GlobalJsonStore.writeOpenedBefore()
                                                backStack.add(MainScreen)
                                            },
                                            {
                                                backStack.removeLastOrNull()
                                            }
                                        )
                                    }
                                }
                                is MainScreen -> {
                                    NavEntry(
                                        key = key,
                                    ) {
                                        MainTodoScreen(
                                            onSettingsButtonClick = {
                                                backStack.add(MainSettingsScreen)
                                            },
                                            onAppBlockListButtonClick = {
                                                backStack.add(AppBlockListScreen)
                                            },
                                            stopForegroundService = {
                                                stopBlocking()
                                            },
                                            tasksAreCompleted = {
                                                stopBlocking()
                                            },
                                            tasksAreNotCompleted = {
                                                startBlocking()
                                            },
                                            onRequestBreak = {
                                                Intent(this, BlockingService::class.java)
                                                    .putExtra("break_time_minutes_extra", current.value[BREAK_TIME] ?: BREAK_TIME_DEFAULT)
                                                    .also {
                                                        it.action = Actions.REQUEST_BREAK.toString()
                                                        startService(it)
                                                    }
                                            }
                                        )
                                    }
                                }
                                is MainSettingsScreen -> {
                                    NavEntry(
                                        key = key
                                    ) {
                                        MainSettingsScreen(
                                            { clicked -> backStack.removeLastOrNull() },
                                            { backStack.add(BreakSettingsScreen) },
                                            { backStack.add(PermissionsScreen) },
                                        )
                                    }
                                }
                                is AppBlockListScreen -> {
                                    NavEntry(
                                        key = key
                                    ) {
                                        AppBlockList(
                                            { clicked -> backStack.removeLastOrNull() },
                                            { sendBlockedAppListUpdate() }
                                        )
                                    }
                                }
                                is BreakSettingsScreen -> {
                                    NavEntry(
                                        key = key
                                    ) {
                                        BreakSettingsScreen { clicked -> backStack.removeLastOrNull() }
                                    }
                                }
                                else -> throw RuntimeException("Invalid NavKey.")
                            }
                        }
                    )
                }
            }
        }
    }

    fun startBlocking(): Intent {
        return Intent(applicationContext, BlockingService::class.java)
            .putExtra("blocked_apps_json_string_extra", GlobalJsonStore.getBlockedAppPackageNameString())
            .also {
                it.action = Actions.START_BLOCKING.toString()
                startService(it)
            }
    }

    fun stopBlocking(): Intent {
        return Intent(applicationContext, BlockingService::class.java).also {
            it.action = Actions.STOP_BLOCKING.toString()
            startService(it)
        }
    }

    fun sendBlockedAppListUpdate(): Intent {
        return Intent(applicationContext, BlockingService::class.java)
            .also {
                it.action = Actions.UPDATE_BLOCKED_APP_LIST.toString()
                startService(it)
            }
    }
}

// https://stackoverflow.com/a/65243835
fun Context.getActivityOrNull(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}