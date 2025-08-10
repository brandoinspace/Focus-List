package space.brandoin.focuslist

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import kotlinx.serialization.Serializable
import space.brandoin.focuslist.data.BlockedAppsJSONStore
import space.brandoin.focuslist.data.TasksJSONStore
import space.brandoin.focuslist.screens.AppBlockList
import space.brandoin.focuslist.screens.MainTodoScreen
import space.brandoin.focuslist.ui.theme.FocusListTheme


// https://www.youtube.com/watch?v=jhrfx8Uk_y0
@Serializable
object MainScreen: NavKey

@Serializable
object SettingsScreen: NavKey

@Serializable
object AppBlockListScreen: NavKey

// TODO: Permission allow screen
// TODO: Block apps
// TODO: Easily editable names
// TODO: Separate list (at bottom) for completed tasks(maybe)
// TODO: animations
// TODO: save data
// TODO: keyboard focus
// TODO: settings
// TODO: Scroll padding around toolbar
// TODO: see if material 3 expressive works for older android versions
// TODO: put all strings into Resources
// TODO: task widget
// TODO: task shortcuts
// TODO: auto start app
// TODO: battery optimisation?
// TODO: proper exception handling
// TODO: animation for when block screen in removed
// TODO: test what happens if blocked app is uninstalled and reinstalled
// TODO: combine both JSON stores
// TODO: add service status to rerun service if not running instead of using add button
// https://developer.android.com/develop/ui/views/components/settings
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // TODO: proper permission requests
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            0
        )

        BlockedAppsJSONStore.filesDir = this.filesDir
        TasksJSONStore.filesDir = this.filesDir

        setContent {
            FocusListTheme {
                val backStack = rememberNavBackStack(MainScreen)

                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                        rememberSceneSetupNavEntryDecorator()
                    ),
                    entryProvider = { key ->
                        when(key) {
                            is MainScreen -> {
                                NavEntry(
                                    key = key,
                                ) {
                                    MainTodoScreen(
                                        onSettingsButtonClick = {
                                            backStack.add(SettingsScreen)
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
                                        }
                                    )
                                }
                            }
                            is SettingsScreen -> {
                                NavEntry(
                                    key = key
                                ) {
                                    Text("Test")
                                }
                            }
                            is AppBlockListScreen -> {
                                NavEntry(
                                    key = key
                                ) {
                                    FocusListTheme {
                                        AppBlockList(
                                            { clicked -> backStack.removeLastOrNull() },
                                            { sendBlockedAppListUpdate() }
                                        )
                                    }
                                }
                            }
                            else -> throw RuntimeException("Invalid NavKey.")
                        }
                    }
                )
            }
        }
    }

    fun startBlocking(): Intent {
        return Intent(applicationContext, BlockingService::class.java)
            .putExtra("blocked_apps_json_string_extra", BlockedAppsJSONStore.getBlockedAppPackageNameString())
            .also {
                it.action = BlockingService.Actions.START_BLOCKING.toString()
                startService(it)
            }
    }

    fun stopBlocking(): Intent {
        return Intent(applicationContext, BlockingService::class.java).also {
            it.action = BlockingService.Actions.STOP_BLOCKING.toString()
            startService(it)
        }
    }

    fun sendBlockedAppListUpdate(): Intent {
        return Intent(applicationContext, BlockingService::class.java)
            .putExtra("updated_blocked_apps_json_string_extra", BlockedAppsJSONStore.getBlockedAppPackageNameString())
            .also {
            it.action = BlockingService.Actions.UPDATE_BLOCKED_APP_LIST.toString()
            startService(it)
        }
    }
}