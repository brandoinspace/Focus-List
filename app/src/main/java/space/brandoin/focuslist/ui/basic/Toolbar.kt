package space.brandoin.focuslist.ui.basic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.zhanghai.compose.preference.LocalPreferenceFlow
import space.brandoin.focuslist.COOLDOWN_ALARM_INTENT
import space.brandoin.focuslist.R
import space.brandoin.focuslist.screens.ALLOW_BREAKS
import space.brandoin.focuslist.screens.ALLOW_BREAKS_DEFAULT

@Composable
fun TodoFAB(
    onClickAdd: () -> Unit,
    isCurrentlyEditing: Boolean,
    onClickFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedRotation by animateFloatAsState(if (isCurrentlyEditing) 90f else 0f)
    LargeFloatingActionButton(
        onClick = {
            if (isCurrentlyEditing) {
                onClickFinish()
            } else {
                onClickAdd()
            }
        },
        shape = MaterialShapes.Sunny.toShape(),
        containerColor = FloatingToolbarDefaults.vibrantFloatingToolbarColors().fabContainerColor,
        modifier = Modifier.rotate(animatedRotation)
    ) {
        val mod = modifier.size(36.dp).rotate(-animatedRotation)
        if (isCurrentlyEditing) {
            Icon(Icons.Filled.Check, "Finish", mod)
        } else {
            Icon(Icons.Filled.Add, "Add", mod)
        }
    }
}

@Composable
fun Toolbar(
    openBreakAlert: () -> Unit,
    removeAllTasks: () -> Unit,
    onSettingsButtonClick: () -> Unit,
    onAppBlockListButtonClick: () -> Unit,
    onReorderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val current = LocalPreferenceFlow.current
    val allowBreaks: Boolean = current.value[ALLOW_BREAKS] ?: ALLOW_BREAKS_DEFAULT
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier.offset(x = (-8).dp, y = FloatingToolbarDefaults.ScreenOffset),
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        content = {
            if (allowBreaks) {
                IconButton(onClick = openBreakAlert, enabled = COOLDOWN_ALARM_INTENT == null) {
                    Icon(Icons.Rounded.MoreTime, "Take a Break")
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
            IconButton(onClick = onReorderClick) {
                Icon(Icons.Filled.Edit, "Edit Order")
            }
            IconButton(onClick = onSettingsButtonClick) {
                Icon(Icons.Filled.Settings, "Settings")
            }
        }
    )
}