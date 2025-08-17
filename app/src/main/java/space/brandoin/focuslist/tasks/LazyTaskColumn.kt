package space.brandoin.focuslist.tasks

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.brandoin.focuslist.viewmodels.TasksViewModel

@Composable
fun LazyTaskColumn(
    tasksAreCompleted: () -> Unit,
    tasksAreNotCompleted: () -> Unit,
    viewModel: TasksViewModel = viewModel(),
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(
            items = viewModel.tasks,
            key = { task -> task.id },
        ) { task ->
            TaskTodo(
                task,
                { viewModel.removeTask(task) },
                Modifier.animateItem(
                    placementSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) { c ->
                viewModel.completeTask(task, c)
                if (viewModel.areAllTasksCompleted()) {
                    tasksAreCompleted()
                } else {
                    tasksAreNotCompleted()
                }
            }
        }
    }
}