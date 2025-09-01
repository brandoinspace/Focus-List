package space.brandoin.focuslist.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoorBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import space.brandoin.focuslist.alerts.DisplayOverPermissionDialog
import space.brandoin.focuslist.alerts.NotificationPermissionDialog
import space.brandoin.focuslist.getActivityOrNull

@Composable
fun PermissionScreen(
    firstTime: Boolean,
    finishFirstTime: (Boolean) -> Unit,
    backToTodo: (Boolean) -> Unit,
) {
    val current = LocalContext.current
    var refreshChecks by rememberSaveable { mutableStateOf(false) }
    var takeToSettings by rememberSaveable { mutableStateOf(false) }
    var showNotificationPermissionDialog by rememberSaveable { mutableStateOf(false) }
    val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { res ->
            val activity = current.getActivityOrNull()
            if (!res) {
                if (activity != null) {
                    if (!shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                        takeToSettings = true
                    }
                    showNotificationPermissionDialog = true
                }
            }
        }
    )
    var showDisplayPermissionDialog by rememberSaveable { mutableStateOf(false) }
    val displayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { res ->
            val activity = current.getActivityOrNull()
            if (!res) {
                if (activity != null) {
                    if (!shouldShowRequestPermissionRationale(activity, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                        takeToSettings = true
                    }
                    showDisplayPermissionDialog = true
                }
            }
        }
    )
    SettingsScreenTemplate(
        "Permissions",
        {
            IconToggleButton(
                checked = false,
                onCheckedChange = { refreshChecks = it },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Outlined.Refresh, "Refresh Changes")
            }
            IconToggleButton(
                checked = false,
                onCheckedChange = { clicked ->
                    if (firstTime) {
                        finishFirstTime(clicked)
                    } else {
                        backToTodo(clicked)
                    }
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Outlined.DoorBack, "Back to Focus List")
            }
        },
        {
            if (showNotificationPermissionDialog) {
                NotificationPermissionDialog({ showNotificationPermissionDialog = false }) {
                    showNotificationPermissionDialog = false
                    if (takeToSettings) {
                        Intent(
                            Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                        ).putExtra(Settings.EXTRA_APP_PACKAGE, current.packageName).also {
                            current.startActivity(it)
                        }
                    } else {
                        postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
            if (showDisplayPermissionDialog) {
                DisplayOverPermissionDialog({ showDisplayPermissionDialog = false }) {
                    showDisplayPermissionDialog = false
                    if (takeToSettings) {
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        ).also {
                            current.startActivity(it)
                        }
                    } else {
                        displayPermissionLauncher.launch(Manifest.permission.SYSTEM_ALERT_WINDOW)
                    }
                }
            }
        },
        {
            Column(Modifier.fillMaxSize().padding(8.dp)) {
                Row(Modifier.clickable {
                    val permission = Manifest.permission.POST_NOTIFICATIONS
                    val isGranted = checkSelfPermission(current, permission) == PackageManager.PERMISSION_GRANTED
                    val activity = current.getActivityOrNull()
                    if (activity != null) {
                        if (!isGranted) {
                            if (shouldShowRequestPermissionRationale(activity, permission)) {
                                showNotificationPermissionDialog = true
                            } else {
                                postNotificationPermissionLauncher.launch(permission)
                            }
                        }
                    }
                }) {
                    Column(Modifier.weight(1f).padding(16.dp)) {
                        Text("Post Notifications")
                        Text(
                            "Sends notifications when breaks are over or when the blocking service starts. Press to enable/disable permission.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    key(refreshChecks) {
                        if (refreshChecks) refreshChecks = false
                        Checkbox(
                            checked = checkSelfPermission(current, Manifest.permission.POST_NOTIFICATIONS)
                                    == PackageManager.PERMISSION_GRANTED,
                            onCheckedChange = null,
                            modifier = Modifier.padding(16.dp).align(Alignment.CenterVertically)
                        )
                    }
                }
                Row(Modifier.clickable {
                    val permission = Manifest.permission.SYSTEM_ALERT_WINDOW
                    val isGranted = Settings.canDrawOverlays(current)
                    val activity = current.getActivityOrNull()
                    if (activity != null) {
                        if (!isGranted) {
                            if (shouldShowRequestPermissionRationale(activity, permission)) {
                                showDisplayPermissionDialog = true
                            } else {
                                displayPermissionLauncher.launch(permission)
                            }
                        }
                    }
                }) {
                    Column(Modifier.weight(1f).padding(16.dp)) {
                        Text("Display Over Other Apps")
                        Text(
                            "Gives Focus List the ability to display the block screen over apps. Press to enable/disable permission.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    key(refreshChecks) {
                        if (refreshChecks) refreshChecks = false
                        Checkbox(
                            checked = Settings.canDrawOverlays(current),
                            onCheckedChange = null,
                            modifier = Modifier.padding(16.dp).align(Alignment.CenterVertically)
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(16.dp))
                Row {
                    Column(Modifier.weight(1f).padding(16.dp)) {
                        Text("Query All Packages")
                        Text(
                            "Reads what apps are on your device so they can be individually blocked as chosen by the user.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Row {
                    Column(Modifier.weight(1f).padding(16.dp)) {
                        Text("Vibrate")
                        Text(
                            "Allows the device to vibrate for haptic feedback.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Row {
                    Column(Modifier.weight(1f).padding(16.dp)) {
                        Text("Foreground Service (Special Use)")
                        Text(
                            "Runs the blocking service in the background while Focus List is closed.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )
}