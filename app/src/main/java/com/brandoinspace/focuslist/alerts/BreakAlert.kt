package com.brandoinspace.focuslist.alerts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brandoinspace.focuslist.custom.fromMinsToString
import com.brandoinspace.focuslist.screens.BREAK_COOLDOWN
import com.brandoinspace.focuslist.screens.BREAK_TIME
import com.brandoinspace.focuslist.screens.BREAK_TIME_DEFAULT
import me.zhanghai.compose.preference.LocalPreferenceFlow

@Composable
fun BreakAlert(
    openBreakAlert: () -> Unit,
    onRequestBreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val current = LocalPreferenceFlow.current
    val breakString = fromMinsToString((current.value[BREAK_TIME] ?: BREAK_TIME_DEFAULT))
    val cooldown = (current.value[BREAK_COOLDOWN] ?: BREAK_TIME_DEFAULT)
    val cooldownString = fromMinsToString(cooldown)
    BasicAlertDialog(
        onDismissRequest = openBreakAlert
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Icon(
                    Icons.Rounded.MoreTime,
                    "Take a Break",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(24.dp)
                )
                Spacer(modifier.height(16.dp))
                Text(
                    "Take a Break?",
                    Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                val cooldownText =
                    if (cooldown == 0) "." else " and breaks will be disabled for $cooldownString."
                Text(
                    "It is good to take breaks. When you start your break, your apps will be unlocked for $breakString."
                            + " After the time period, your apps will lock again" + cooldownText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier.align(Alignment.End)) {
                    TextButton(
                        onClick = openBreakAlert,
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            openBreakAlert()
                            onRequestBreak()
                        },
                    ) {
                        Text("Take a Break")
                    }
                }
            }
        }
    }
}