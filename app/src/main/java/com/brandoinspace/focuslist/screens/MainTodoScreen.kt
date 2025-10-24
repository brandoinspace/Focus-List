package com.brandoinspace.focuslist.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
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
import com.brandoinspace.focuslist.BREAK_ALARM_INTENT
import com.brandoinspace.focuslist.BlockingService
import com.brandoinspace.focuslist.BlockingService.Actions
import com.brandoinspace.focuslist.addTaskShortcut
import com.brandoinspace.focuslist.alerts.AccessibilityAlert
import com.brandoinspace.focuslist.alerts.BreakAlert
import com.brandoinspace.focuslist.alerts.BreakCooldownAlert
import com.brandoinspace.focuslist.alerts.CancelBreakAlert
import com.brandoinspace.focuslist.alerts.NewTaskDialog
import com.brandoinspace.focuslist.alerts.RenameTaskAlert
import com.brandoinspace.focuslist.alerts.ServiceAlert
import com.brandoinspace.focuslist.data.AppViewModelProvider
import com.brandoinspace.focuslist.data.GlobalJsonStore
import com.brandoinspace.focuslist.data.tasks.TaskEntity
import com.brandoinspace.focuslist.data.tasks.TasksViewModel
import com.brandoinspace.focuslist.data.tasks.getPercentage
import com.brandoinspace.focuslist.requestBreakShortcut
import com.brandoinspace.focuslist.tasks.LazyTaskColumn
import com.brandoinspace.focuslist.tasks.tempListStore
import com.brandoinspace.focuslist.ui.basic.Header
import com.brandoinspace.focuslist.ui.basic.HintText
import com.brandoinspace.focuslist.ui.basic.TodoFAB
import com.brandoinspace.focuslist.ui.basic.Toolbar
import com.brandoinspace.focuslist.ui.theme.FocusListTheme
import kotlinx.coroutines.launch

@Composable
fun MainTodoScreen(
    onSettingsButtonClick: () -> Unit,
    onAppBlockListButtonClick: () -> Unit,
    stopForegroundService: () -> Unit,
    tasksAreCompleted: () -> Unit,
    tasksAreNotCompleted: () -> Unit,
    onRequestBreak: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory)
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
    var openAccessibilityAlert by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    // TODO: wait until tasks are loaded first
    val state by viewModel.tasks.collectAsState()

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
                        state,
                        { openServiceAlert = true },
                        { openCancelBreakAlert = true },
                        { openBreakCooldownAlert = true },
                        { openAccessibilityAlert = true }
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
                                .putExtra(
                                    "blocked_apps_json_string_extra",
                                    GlobalJsonStore.getBlockedAppPackageNameString()
                                )
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
                    if (openAccessibilityAlert) {
                        AccessibilityAlert({ openAccessibilityAlert = false }) {
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).also {
                                current.startActivity(it)
                                openAccessibilityAlert = false
                            }
                        }
                    }
                    if (openNameDialog || addTaskShortcut) {
                        NewTaskDialog(
                            { openNameDialog = false; addTaskShortcut = false }
                        ) {
                            coroutineScope.launch {
                                viewModel.saveTask(
                                    TaskEntity(0, it, false, 0)
                                )
                                if (viewModel.allTasksCompleted()) {
                                    tasksAreCompleted()
                                } else {
                                    tasksAreNotCompleted()
                                }
                                GlobalJsonStore.writePercentageJSON(getPercentage(viewModel.tasks.value.tasks))
                                // https://github.com/google-developer-training/basic-android-kotlin-compose-training-inventory-app/blob/e0773b718f2670e401c039ee965879c5e88ca424/app/src/main/java/com/example/inventory/ui/home/HomeScreen.kt
                            }
                        }
                    }
                    if (openRenameDialog && currentlyRenaming != -1) {
                        RenameTaskAlert(state, currentlyRenaming, {
                            currentlyRenaming = -1
                            openRenameDialog = false
                        })
                    }

                    LazyTaskColumn(
                        state,
                        tasksAreCompleted,
                        tasksAreNotCompleted,
                        {
                            openRenameDialog = true
                            currentlyRenaming = it
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        },
                        editingOrder,
                    )

                    if (state.tasks.isEmpty()) {
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
                                coroutineScope.launch {
                                    viewModel.dropTasks()
                                }
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
                            coroutineScope.launch {
                                viewModel.updateAllTasks(tempListStore)
                            }
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