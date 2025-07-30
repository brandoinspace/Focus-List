package space.brandoin.focuslist.ui.basic

import android.content.Intent
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
import space.brandoin.focuslist.BlockingService
import space.brandoin.focuslist.R

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
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier.offset(x = (-8).dp, y = FloatingToolbarDefaults.ScreenOffset),
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        content = {
            IconButton(
                onClick = onSettingsButtonClick
            ) {
                Icon(Icons.Filled.Settings, "Settings")
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
            IconButton(onClick = openBreakAlert) {
                Icon(Icons.Filled.HistoryToggleOff, "Take a Break")
            }
        }
    )
}

@Composable
fun BreakAlert(openBreakAlert: () -> Unit, modifier: Modifier = Modifier) {
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
                    Icons.Filled.HistoryToggleOff,
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
                Text(
                    "It is good to take breaks. When you start your break, your apps will be unlocked for 5 minutes."
                            + " After the time period, your apps will lock again and breaks will be disabled for 30 minutes.",
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
                        onClick = openBreakAlert,
                    ) {
                        Text("Take a Break")
                    }
                }
            }
        }
    }
}