package com.brandoinspace.focuslist.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.brandoinspace.focuslist.R
import com.brandoinspace.focuslist.data.tasks.TaskEntity
import com.brandoinspace.focuslist.data.tasks.TasksViewModel
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
fun TaskTodo(
    modifier: Modifier = Modifier,
    task: TaskEntity,
    onRemoveSwipe: (TaskEntity) -> Unit,
    onClickToRename: (Int) -> Unit,
    elevation: Dp,
    scope: ReorderableCollectionItemScope,
    isReordering: Boolean,
    tasksCompleted: () -> Unit,
    tasksNotCompleted: () -> Unit,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val color = if (!task.completed) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val border = if (!task.completed) {
        null
    } else {
        BorderStroke(1.dp, SolidColor(MaterialTheme.colorScheme.outlineVariant))
    }
    val textColor = if (!task.completed) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val state by viewModel.tasks.collectAsState()
    val isTop = if (usingTempListStore) {
        tempListStore.indexOf(task) == 0
    } else {
        task.listOrder == 0
    }
    val animatedTop by animateDpAsState(
        if (isTop) {
            28.dp
        } else {
            8.dp
        }
    )
    val isBottom = if (usingTempListStore) {
        tempListStore.indexOf(task) == (tempListStore.size - 1)
    } else {
        task.listOrder == (state.tasks.size - 1)
    }
    val animatedBottom by animateDpAsState(
        if (isBottom) {
            28.dp
        } else {
            8.dp
        }
    )
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var resettingSwipe by remember { mutableStateOf(false) }
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surfaceContainer
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.secondaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                }
            )
            Column(Modifier.fillMaxSize()) {
                Surface(
                    color = color,
                    shape = RoundedCornerShape(
                        topStart = animatedTop,
                        topEnd = animatedTop,
                        bottomStart = animatedBottom,
                        bottomEnd = animatedBottom
                    )
                ) {
                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
                            Icon(
                                Icons.Filled.DeleteSweep,
                                "Delete Task",
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(end = 24.dp)
                                    .size(40.dp)
                            )
                        }
                    }
                    if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                        Row(Modifier.fillMaxSize()) {
                            Icon(
                                painter = painterResource(R.drawable.sweep_google_font),
                                "Complete Task",
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 24.dp)
                                    .size(40.dp)
                            )
                        }
                    }
                }
            }
        },
        onDismiss = { direction ->
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                onRemoveSwipe(task)
                checkComplete(
                    viewModel,
                    task,
                    tasksCompleted,
                    tasksNotCompleted
                ) {}
                dismissState.reset()
            } else {
                if (!resettingSwipe) {
                    checkComplete(
                        viewModel,
                        task,
                        tasksCompleted,
                        tasksNotCompleted
                    ) {
                        resettingSwipe = true
                    }
                }
                dismissState.reset()
                resettingSwipe = false
            }
        }
    ) {
        Surface(
            color = color,
            modifier = Modifier.height(80.dp),
            shape = RoundedCornerShape(
                topStart = animatedTop,
                topEnd = animatedTop,
                bottomStart = animatedBottom,
                bottomEnd = animatedBottom
            ),
            tonalElevation = 8.dp,
            border = border,
            shadowElevation = elevation
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .clickable(
                            true,
                            onClick = { onClickToRename(task.id) },
                            onClickLabel = "Rename Task"
                        ),
                    text = task.name,
                    color = textColor
                )
                if (isReordering) {
                    IconButton(
                        modifier = with(scope) { Modifier.draggableHandle() },
                        onClick = {}
                    ) {
                        Icon(Icons.Rounded.DragHandle, "Reorder")
                    }
                } else {
                    FilledIconToggleButton(
                        checked = !task.completed,
                        onCheckedChange = {
                            coroutineScope.launch {
                                checkComplete(
                                    viewModel,
                                    task,
                                    tasksCompleted,
                                    tasksNotCompleted
                                ) {}
                            }
                        },
                        shapes = IconToggleButtonShapes(
                            shape = MaterialShapes.Cookie4Sided.toShape(),
                            pressedShape = MaterialShapes.Square.toShape(),
                            checkedShape = MaterialShapes.Square.toShape()
                        ),
                        modifier = Modifier.padding(end = 8.dp),
                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    ) {
                        if (!task.completed) {
                            Icon(
                                Icons.Filled.Check,
                                "Complete Task"
                            )
                        } else {
                            Icon(
                                Icons.Filled.RemoveDone,
                                "Task Completed",
                                Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun checkComplete(
    viewModel: TasksViewModel,
    task: TaskEntity,
    tasksCompleted: () -> Unit,
    tasksNotCompleted: () -> Unit,
    setReset: () -> Unit,
) {
    setReset()
    viewModel.updateCompletion(task, !task.completed)
    if (viewModel.allTasksCompleted()) {
        tasksCompleted()
    } else {
        tasksNotCompleted()
    }
}