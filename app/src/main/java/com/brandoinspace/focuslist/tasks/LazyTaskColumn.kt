package com.brandoinspace.focuslist.tasks

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.brandoinspace.focuslist.data.GlobalJsonStore
import com.brandoinspace.focuslist.data.tasks.TaskEntity
import com.brandoinspace.focuslist.data.tasks.TasksViewModel
import com.brandoinspace.focuslist.data.tasks.TasksWrapper
import com.brandoinspace.focuslist.data.tasks.getPercentage
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

var tempListStore: SnapshotStateList<TaskEntity> = mutableStateListOf()
var usingTempListStore by mutableStateOf(false)

@Composable
fun LazyTaskColumn(
    state: TasksWrapper,
    tasksAreCompleted: () -> Unit,
    tasksAreNotCompleted: () -> Unit,
    onClickToRename: (Int) -> Unit,
    isReordering: Boolean,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val list = state.tasks.sortedBy { it.listOrder }.toMutableStateList()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        usingTempListStore = true
        list.add(to.index, list.removeAt(from.index))
        for (t in list) {
            t.listOrder = list.indexOf(t)
        }
        tempListStore = list.sortedBy { it.listOrder }.toMutableStateList()
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        usingTempListStore = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(list, key = { _, item -> item.id }) { index, task ->
            ReorderableItem(
                reorderableLazyListState,
                key = task.id,
                animateItemModifier = Modifier.animateItem(
                    placementSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                val coroutineScope = rememberCoroutineScope()
                TaskTodo(
                    Modifier,
                    task,
                    { task ->
                        coroutineScope.launch {
                            viewModel.removeTask(task)
                            GlobalJsonStore.writePercentageJSON(getPercentage(viewModel.tasks.value.tasks))
                        }
                    },
                    onClickToRename,
                    elevation,
                    this@ReorderableItem,
                    isReordering,
                    tasksAreCompleted,
                    tasksAreNotCompleted
                )
            }
        }

        item {
            Spacer(Modifier.padding(top = 128.dp))
        }
    }
}