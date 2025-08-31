package space.brandoin.focuslist.tasks

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import space.brandoin.focuslist.viewmodels.TasksViewModel

@Composable
fun LazyTaskColumn(
    tasksAreCompleted: () -> Unit,
    tasksAreNotCompleted: () -> Unit,
    onClickToRename: (Int) -> Unit,
    isReordering: Boolean,
    viewModel: TasksViewModel = viewModel(),
) {
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.reorderNoSave(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(viewModel.taskIds, key = { it }) {
            ReorderableItem(reorderableLazyListState, key = it) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                TaskTodo(
                    viewModel.findTask(it),
                    { task ->
                        viewModel.removeTask(task)
                    },
                    Modifier.animateItem(
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    onClickToRename,
                    elevation,
                    this@ReorderableItem,
                    isReordering
                ) { c ->
                    viewModel.completeTask(viewModel.findTask(it), c)
                    if (viewModel.areAllTasksCompleted()) {
                        tasksAreCompleted()
                    } else {
                        tasksAreNotCompleted()
                    }
                }
            }
        }

        item {
            Spacer(Modifier.padding(top = 128.dp))
        }
    }
}