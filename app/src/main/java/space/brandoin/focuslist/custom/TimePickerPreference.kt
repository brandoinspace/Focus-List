package space.brandoin.focuslist.custom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.zhanghai.compose.preference.LocalPreferenceTheme
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.rememberPreferenceState

inline fun LazyListScope.timeInputPreference(
    key: String,
    defaultValue: Int,
    crossinline title: @Composable (Int) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    crossinline rememberState: @Composable () -> MutableState<Int> = {
        rememberPreferenceState(key, defaultValue)
    },
    enabled: Boolean = true,
    noinline icon: @Composable (() -> Unit)? = null,
    noinline summary: @Composable (() -> Unit)? = null,
    isZeroTimeAllowed: Boolean = false
) {
    item(key = key, contentType = "TimePickerPreference") {
        val state = rememberState()
        val value by state
        TimeInputPreference(
            state = state,
            title = { title(value) },
            modifier = modifier,
            enabled = enabled,
            icon = icon?.let { { it } },
            summary = summary?.let { { it } },
            isZeroTimeAllowed = isZeroTimeAllowed
        )
    }
}

@Composable
fun TimeInputPreference(
    state: MutableState<Int>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    isZeroTimeAllowed: Boolean = false
) {
    var value by state
    TimeInputPreference(
        value = value,
        onValueChange = { value = it },
        title = title,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        summary = summary,
        isZeroTimeAllowed = isZeroTimeAllowed
    )
}

@Composable
fun TimeInputPreference(
    value: Int,
    onValueChange: (Int) -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    isZeroTimeAllowed: Boolean = false
) {
    var openDialog by rememberSaveable { mutableStateOf(false) }
    Preference(
        title = title,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        summary = summary,
        widgetContainer = {
            val theme = LocalPreferenceTheme.current
            Text(fromMinsToString(value), modifier = Modifier.padding(theme.padding))
        }
    ) {
        openDialog = true
    }
    if (openDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = fromMins(value).first,
            initialMinute = fromMins(value).second,
            is24Hour = true
        )
        BasicAlertDialog(
            onDismissRequest = { openDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(Modifier.padding(24.dp)) {
                    Icon(
                        Icons.Filled.HistoryToggleOff,
                        "Set Break Time",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(24.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    TimeInput(timePickerState)
                    Spacer(Modifier.height(24.dp))
                    var enableTime = true
                    if ((!isZeroTimeAllowed) && timePickerState.minute == 0 && timePickerState.hour == 0) enableTime = false
                    Row(Modifier.align(Alignment.End)) {
                        TextButton(
                            onClick = { openDialog = false },
                        ) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                openDialog = false
                                onValueChange(toMins(timePickerState.hour, timePickerState.minute))
                            },
                            enabled = enableTime
                        ) {
                            Text("Set Time")
                        }
                    }
                }
            }
        }
    }
}

fun fromMins(mins: Int): Pair<Int, Int> {
    val hours = mins / 60f
    val minutes = (hours - hours.toInt()) * 60
    return Pair(hours.toInt(), minutes.toInt())
}

private fun toMins(hours: Int, mins: Int): Int {
    return (hours * 60) + mins
}

fun fromMinsToString(mins: Int): String {
    val pair = fromMins(mins)
    var time = ""
    if (pair.first != 0) {
        val number = if (pair.first == 1) "hour" else "hours"
        time += "${pair.first} $number"
    }
    if (pair.first != 0 && pair.second != 0) {
        time += " and "
    }
    if (pair.second != 0) {
        time += "${pair.second} minutes"
    }
    if (pair.first == 0 && pair.second == 0) {
        time = "No time set"
    }
    return time
}