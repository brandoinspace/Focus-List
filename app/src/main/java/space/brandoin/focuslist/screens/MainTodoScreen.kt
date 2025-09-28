package space.brandoin.focuslist.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.zhanghai.compose.preference.LocalPreferenceFlow
import space.brandoin.focuslist.BREAK_ALARM_INTENT
import space.brandoin.focuslist.BlockingService
import space.brandoin.focuslist.BlockingService.Actions
import space.brandoin.focuslist.addTaskShortcut
import space.brandoin.focuslist.alerts.BreakAlert
import space.brandoin.focuslist.alerts.BreakCooldownAlert
import space.brandoin.focuslist.alerts.CancelBreakAlert
import space.brandoin.focuslist.alerts.NewTaskDialog
import space.brandoin.focuslist.alerts.RenameTaskAlert
import space.brandoin.focuslist.alerts.ServiceAlert
import space.brandoin.focuslist.data.GlobalJsonStore
import space.brandoin.focuslist.requestBreakShortcut
import space.brandoin.focuslist.tasks.LazyTaskColumn
import space.brandoin.focuslist.ui.basic.Header
import space.brandoin.focuslist.ui.basic.HintText
import space.brandoin.focuslist.ui.basic.TodoFAB
import space.brandoin.focuslist.ui.basic.Toolbar
import space.brandoin.focuslist.ui.theme.FocusListTheme
import space.brandoin.focuslist.viewmodels.TasksViewModel

@Composable
fun MainTodoScreen(
    onSettingsButtonClick: () -> Unit,
    onAppBlockListButtonClick: () -> Unit,
    stopForegroundService: () -> Unit,
    tasksAreCompleted: () -> Unit,
    tasksAreNotCompleted: () -> Unit,
    onRequestBreak: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel()
) {
    val current = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    var openBreakAlert by rememberSaveable { mutableStateOf(false) }
    var openNameDialog by rememberSaveable { mutableStateOf(false) }
    var openRenameDialog by rememberSaveable { mutableStateOf(false) }
    var currentlyRenaming by rememberSaveable { mutableIntStateOf(-1) }
    var editingOrder by rememberSaveable { mutableStateOf(false) }
    var openServiceAlert by rememberSaveable { mutableStateOf(false) }
    var openCancelBreakAlert by rememberSaveable { mutableStateOf(false) }
    var openBreakCooldownAlert by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Box {
            Column {
                Column(
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(top = 36.dp)
                ) {
                    Header(
                        { openServiceAlert = true },
                        { openCancelBreakAlert = true },
                        { openBreakCooldownAlert = true }
                    )
                }
                Surface(
                    modifier = modifier.fillMaxSize(),
                    shape = RoundedCornerShape(28.dp, 28.dp, 0.dp, 0.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    if (openServiceAlert) {
                        ServiceAlert({ openServiceAlert = false }) {
                            openServiceAlert = false
                            Intent(current.applicationContext, BlockingService::class.java)
                                .putExtra("blocked_apps_json_string_extra", GlobalJsonStore.getBlockedAppPackageNameString())
                                .also {
                                    it.action = Actions.START_BLOCKING.toString()
                                    current.startService(it)
                                }
                        }
                    }
                    if (openCancelBreakAlert) {
                        CancelBreakAlert({ openCancelBreakAlert = false }) {
                            openCancelBreakAlert = false
                            Intent(current.applicationContext, BlockingService::class.java)
                                .also {
                                    it.action = Actions.CANCEL_BREAK.toString()
                                    current.startService(it)
                                }
                        }
                    }
                    if (openBreakAlert || requestBreakShortcut) {
                        BreakAlert({
                            if (BREAK_ALARM_INTENT == null) {
                                openBreakAlert = false
                                requestBreakShortcut = false
                            } else {
                                openCancelBreakAlert = false
                            }
                        }, onRequestBreak)
                    }
                    if (openBreakCooldownAlert) {
                        BreakCooldownAlert({ openBreakCooldownAlert = false })
                    }
                    if (openNameDialog || addTaskShortcut) {
                        NewTaskDialog(
                            { openNameDialog = false; addTaskShortcut = false },
                            tasksAreCompleted, tasksAreNotCompleted
                        )
                    }
                    if (openRenameDialog && currentlyRenaming != -1) {
                        RenameTaskAlert(currentlyRenaming, {
                            currentlyRenaming = -1
                            openRenameDialog = false
                        })
                    }

                    LazyTaskColumn(
                        tasksAreCompleted,
                        tasksAreNotCompleted,
                        {
                            openRenameDialog = true
                            currentlyRenaming = it
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        },
                        editingOrder,
                    )

                    if (viewModel.taskIds.isEmpty()) {
                        HintText()
                    }
                }
            }
            Box(
                modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(innerPadding)
            ) {
                Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                    AnimatedVisibility(
                        visible = !editingOrder,
                        enter = slideInVertically(
                            initialOffsetY = { it * 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { it * 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        Toolbar(
                            {
                                if (BREAK_ALARM_INTENT == null) {
                                    openBreakAlert = true
                                } else {
                                    openCancelBreakAlert = true
                                }
                            },
                            {
                                viewModel.clearAll()
                                stopForegroundService()
                            },
                            { onSettingsButtonClick() },
                            { onAppBlockListButtonClick() },
                            { editingOrder = true }
                        )
                    }
                    TodoFAB(
                        { openNameDialog = true },
                        editingOrder,
                        {
                            editingOrder = false
                            viewModel.save()
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun MainTodoScreenPreview() {
    FocusListTheme {
        MainTodoScreen({}, {}, {}, {}, {}, {})
    }
}