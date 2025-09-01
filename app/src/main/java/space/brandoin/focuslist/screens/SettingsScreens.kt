package space.brandoin.focuslist.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.DoorBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.twoTargetSwitchPreference
import space.brandoin.focuslist.custom.timeInputPreference

const val ALLOW_BREAKS = "allow_breaks"
const val BREAK_TIME = "break_time"
const val BREAK_COOLDOWN = "break_cooldown"
const val ALLOW_BREAKS_DEFAULT = true
const val BREAK_TIME_DEFAULT = 5
const val BREAK_COOLDOWN_DEFAULT = 10

@Composable
fun SettingsScreenTemplate(
    title: String,
    goBackButton: @Composable () -> Unit,
    optionalAlert: @Composable () -> Unit,
    settingsColumn: @Composable () -> Unit,
) {
    optionalAlert()
    Scaffold(
        Modifier.fillMaxSize()
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(top = 36.dp)
            ) {
                Row(Modifier.padding(top = 24.dp)) {
                    Text(
                        title,
                        Modifier.padding(horizontal = 12.dp).weight(1f)
                            .align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W800,
                    )
                    goBackButton()
                }
                Surface(
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                    shape = RoundedCornerShape(28.dp, 28.dp, 0.dp, 0.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    settingsColumn()
                }
            }
        }
    }
}

@Composable
fun MainSettingsScreen(
    returnToMainScreenClick: (Boolean) -> Unit,
    toBreakSettingsClick: (Boolean) -> Unit,
    openPermissionsClick: () -> Unit,
) {
    SettingsScreenTemplate(
        "Settings",
        {
        IconToggleButton(
            checked = false,
            onCheckedChange = { clicked -> returnToMainScreenClick(clicked) },
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Icon(Icons.Outlined.DoorBack, "Back to Focus List")
        }
    },
        {},
        {
            LazyColumn(Modifier.fillMaxSize()) {
                twoTargetSwitchPreference(
                    key = ALLOW_BREAKS,
                    defaultValue = ALLOW_BREAKS_DEFAULT,
                    title = { Text("Breaks") },
                    icon = { Icon(Icons.Filled.HistoryToggleOff, "Allow breaks?") },
                    summary = { if (it) Text("Breaks are allowed.") else Text("Breaks are not allowed.") },
                    onClick = toBreakSettingsClick,
                    enabled = { it },
                    switchEnabled = { true },
                )
                preference(
                    key = "open_permissions",
                    title = { Text("Permissions") },
                    icon = { Icon(Icons.Filled.Shield, "Open Permissions") },
                    onClick = openPermissionsClick
                )
            }
        }
    )
}

@Composable
fun BreakSettingsScreen(
    returnToMainSettings: (Boolean) -> Unit,
) {
    SettingsScreenTemplate(
        "Break Settings",
        {
            IconToggleButton(
                checked = false,
                onCheckedChange = { clicked -> returnToMainSettings(clicked) },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Outlined.DoorBack, "Back to Main Settings")
            }
        },
        {},
        {
            LazyColumn(Modifier.fillMaxSize()) {
                // https://github.com/zhanghai/ComposePreference
                timeInputPreference(
                    BREAK_TIME,
                    defaultValue = BREAK_TIME_DEFAULT,
                    title = { Text("Break Time") },
                )
                timeInputPreference(
                    key = BREAK_COOLDOWN,
                    defaultValue = BREAK_COOLDOWN_DEFAULT,
                    title = { Text("Break Cooldown") },
                    isZeroTimeAllowed = true,
                )
            }
        }
    )
}