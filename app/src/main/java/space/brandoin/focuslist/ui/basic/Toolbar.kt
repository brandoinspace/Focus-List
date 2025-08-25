package space.brandoin.focuslist.ui.basic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.zhanghai.compose.preference.LocalPreferenceFlow
import space.brandoin.focuslist.R
import space.brandoin.focuslist.custom.fromMinsToString
import space.brandoin.focuslist.screens.BREAK_COOLDOWN
import space.brandoin.focuslist.screens.BREAK_TIME
import space.brandoin.focuslist.screens.BREAK_TIME_DEFAULT

@Composable
fun AddTodoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LargeFloatingActionButton(
        onClick = onClick,
        shape = MaterialShapes.Sunny.toShape(),
        containerColor = FloatingToolbarDefaults.vibrantFloatingToolbarColors().fabContainerColor,
    ) {
        Icon(Icons.Filled.Add, "Add", modifier.size(36.dp))
    }
}

@Composable
fun Toolbar(
    openBreakAlert: () -> Unit,
    removeAllTasks: () -> Unit,
    onSettingsButtonClick: () -> Unit,
    onAppBlockListButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val current = LocalPreferenceFlow.current
    val allowBreaks: Boolean = current.value["allow_breaks"] ?: true
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier.offset(x = (-8).dp, y = FloatingToolbarDefaults.ScreenOffset),
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        content = {
            if (allowBreaks) {
                IconButton(onClick = openBreakAlert) {
                    Icon(Icons.Filled.HistoryToggleOff, "Take a Break")
                }
            }
            IconButton(onClick = onAppBlockListButtonClick) {
                Icon(
                    painter = painterResource(R.drawable.category_search_google_font),
                    "Block Apps"
                )
            }
            IconButton(onClick = removeAllTasks) {
                Icon(Icons.Filled.DeleteSweep, "Clear All")
            }
            IconButton(
                onClick = onSettingsButtonClick
            ) {
                Icon(Icons.Filled.Settings, "Settings")
            }
        }
    )
}