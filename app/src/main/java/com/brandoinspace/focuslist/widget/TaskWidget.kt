package com.brandoinspace.focuslist.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.brandoinspace.focuslist.MainActivity
import com.brandoinspace.focuslist.R
import com.brandoinspace.focuslist.data.tasks.TaskEntity
import com.brandoinspace.focuslist.data.tasks.TasksRepository
import com.brandoinspace.focuslist.startBlocking
import com.brandoinspace.focuslist.stopBlocking
import kotlinx.coroutines.launch

val WidgetAction = ActionParameters.Key<String>("action")

class TaskWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val repo = TasksRepository.get(context)
        provideContent {
            val tasks = repo.getAllTasksStream().collectAsState(emptyList()).value.sortedBy { it.listOrder }
            val coroutineScope = rememberCoroutineScope()

            val tasksWrapped = mutableListOf<WidgetTaskWrapper>()
            for (task in tasks) {
                tasksWrapped.add(
                    WidgetTaskWrapper(task.name, task.completed, task.id, task)
                )
            }

            Content(
                tasksWrapped,
                actionStartActivity<MainActivity>(
                    actionParametersOf(
                        WidgetAction to "focuslist.WIDGET_ADD_TASK"
                    )
                )
            ) { task ->
                coroutineScope.launch {
                    if (task.original != null) {
                        repo.updateTask(task.original.copy(completed = !task.completed))
                        if (repo.numberOfIncompleteTasks() == 0) {
                            stopBlocking(context)
                        } else {
                            startBlocking(context)
                        }
                    }
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            Content(
                listOf(
                    WidgetTaskWrapper(
                        "Homework", false, 0
                    ),
                    WidgetTaskWrapper(
                        "Laundry", false, 1
                    ),
                    WidgetTaskWrapper(
                        "Dishes", true, 2
                    )
                ),
                null
            ) {}
        }
    }

    @Composable
    fun Content(
        items: List<WidgetTaskWrapper>,
        addTaskOnClick: Action?,
        markCompleteOnClick: (WidgetTaskWrapper) -> Unit,
    ) {
        Scaffold(
            horizontalPadding = 0.dp,
            backgroundColor = GlanceTheme.colors.surface,
            modifier = GlanceModifier.clickable(
                onClick = actionStartActivity<MainActivity>()
            ),
            titleBar = {
                Row(
                    GlanceModifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tasks",
                        GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            color = GlanceTheme.colors.onBackground,
                        )
                    )
                    if (addTaskOnClick == null) {
                        SquareIconButton(
                            imageProvider = ImageProvider(R.drawable.add_task),
                            "Add Task",
                            {},
                            enabled = false,
                            modifier = GlanceModifier.size(height = 32.dp, width = 42.dp)
                        )
                    } else {
                        SquareIconButton(
                            imageProvider = ImageProvider(R.drawable.add_task),
                            "Add Task",
                            addTaskOnClick,
                            enabled = false,
                            modifier = GlanceModifier.size(height = 32.dp, width = 42.dp)
                        )
                    }
                }
            }
        ) {
            LazyColumn(
                GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.onSecondary)
                    .cornerRadius(12.dp),
            ) {
                item {
                    if (items.isEmpty()) {
                        Box(
                            GlanceModifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                GlanceModifier,
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    GlanceModifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "No Tasks",
                                        style = TextStyle(color = GlanceTheme.colors.outline, textAlign = TextAlign.Center),
                                        modifier = GlanceModifier.fillMaxWidth().padding(top = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                items(items, { it.id.toLong() }) { task ->
                    val bgColor = if (task.completed) GlanceTheme.colors.onSecondary else GlanceTheme.colors.primaryContainer
                    Column(
                        GlanceModifier.padding(horizontal = 4.dp)
                    ) {
                        Spacer(GlanceModifier.height(4.dp))
                        Box(
                            GlanceModifier
                                .background(bgColor)
                                .fillMaxHeight()
                                .cornerRadius(12.dp)
                        ) {
                            val textColor = if (task.completed) GlanceTheme.colors.surfaceVariant else GlanceTheme.colors.onPrimaryContainer
                            Row(
                                GlanceModifier.fillMaxSize().padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    task.name,
                                    style = TextStyle(
                                        color = textColor
                                    ),
                                    modifier = GlanceModifier.defaultWeight().padding(start = 12.dp)
                                )
                                val img = if (task.completed) R.drawable.widget_task_complete_button else R.drawable.widget_task_not_complete_button
                                val color = if (task.completed) GlanceTheme.colors.surfaceVariant else GlanceTheme.colors.primary
                                CircleIconButton(
                                    imageProvider = ImageProvider(img),
                                    "Mark as Complete",
                                    {
                                        markCompleteOnClick(task)
                                    },
                                    backgroundColor = null,
                                    contentColor = color,
                                    modifier = GlanceModifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(GlanceModifier.height(4.dp))
                    }
                }
            }
        }
    }
}

data class WidgetTaskWrapper(val name: String, val completed: Boolean, val id: Int, val original: TaskEntity? = null)