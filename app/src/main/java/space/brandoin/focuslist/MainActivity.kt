package space.brandoin.focuslist

import android.app.Activity
import android.app.ComponentCaller
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
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
import space.brandoin.focuslist.screens.BREAK_COOLDOWN
import space.brandoin.focuslist.screens.BREAK_COOLDOWN_DEFAULT
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

var addTaskShortcut by mutableStateOf(false)
var openBlockListShortcut = false
var requestBreakShortcut by mutableStateOf(false)

// TODO: see if material 3 expressive works for older android versions
// TODO: put all strings into Resources
// TODO: task widget
// TODO: proper exception handling
// TODO: block screen animation stops after opening a second time
// TODO: experiment with more material 3 expressive ui
// TODO: open accessibility page
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntents()
        createDynamicShortcuts()

        GlobalJsonStore.filesDir = this.filesDir

        setContent {
            FocusListTheme {
                ProvidePreferenceLocals {
                    var backStack = rememberNavBackStack(MainScreen)
                    if (GlobalJsonStore.isFirstTime()) {
                        backStack = rememberNavBackStack(PermissionsScreen)
                    }
                    if (openBlockListShortcut) {
                        backStack = rememberNavBackStack(MainScreen, AppBlockListScreen)
                        openBlockListShortcut = false
                    }
                    val current = LocalPreferenceFlow.current

                    Surface {
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
                                                        .putExtra("cooldown_time_minutes_extra_request", current.value[BREAK_COOLDOWN] ?: BREAK_COOLDOWN_DEFAULT)
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
                            },
                            transitionSpec = {
                                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                            },
                            popTransitionSpec = {
                                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                            },
                            predictivePopTransitionSpec = {
                                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                            },

                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        handleIntents()
    }

    private fun handleIntents() {
        when (intent.action) {
            "focuslist.ADD_TASK" -> {
                addTaskShortcut = true
            }

            "focuslist.OPEN_BLOCK_LIST" -> {
                openBlockListShortcut = true
            }

            "focuslist.REQUEST_BREAK" -> {
                requestBreakShortcut = true
            }

            Actions.OPEN_APP.toString() -> {
                startBlocking()
            }
        }
    }

    private fun createDynamicShortcuts() {
        val takeBreak = ShortcutInfoCompat.Builder(applicationContext, "request_break")
            .setShortLabel("Take a Break")
            .setIcon(IconCompat.createWithResource(applicationContext, R.drawable.request_break_shortcut))
            .setIntent(Intent(applicationContext, MainActivity::class.java).also { it.action = "focuslist.REQUEST_BREAK" })
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, takeBreak)
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