package space.brandoin.focuslist.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import space.brandoin.focuslist.R
import space.brandoin.focuslist.ui.theme.FocusListTheme
import space.brandoin.focuslist.viewmodels.Task

@Composable
fun TaskTodo(
    task: Task,
    onRemoveSwipe: () -> Unit,
    onClick: (Boolean) -> Unit
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
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surfaceContainer
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.secondaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                }
            )
            Column(Modifier.fillMaxSize().background(color)) {
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
        },
        onDismiss = { direction ->
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                onRemoveSwipe()
                dismissState.reset()
            } else {
                onClick(!task.completed)
                dismissState.reset()
            }
        }
    ) {
        Surface(
            color = color,
            modifier = Modifier.height(80.dp),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 20.dp,
            border = border
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    text = task.name,
                    color = textColor
                )
                FilledTonalIconToggleButton(
                    checked = !task.completed,
                    onCheckedChange = { onClick(!it) },
                    shapes = IconToggleButtonShapes(
                        shape = MaterialShapes.Cookie4Sided.toShape(),
                        pressedShape = MaterialShapes.Square.toShape(),
                        checkedShape = MaterialShapes.Square.toShape()
                    ),
                    modifier = Modifier.padding(end = 8.dp),
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

@Preview()
@Composable
fun TaskTodoPreview() {
    FocusListTheme {
        TaskTodo(Task(100, "Homework", false), {}, {})
    }
}