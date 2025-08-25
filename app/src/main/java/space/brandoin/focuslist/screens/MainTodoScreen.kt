package space.brandoin.focuslist.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.brandoin.focuslist.alerts.BreakAlert
import space.brandoin.focuslist.ui.basic.AddTodoButton
import space.brandoin.focuslist.ui.basic.Header
import space.brandoin.focuslist.ui.basic.HintText
import space.brandoin.focuslist.ui.basic.Toolbar
import space.brandoin.focuslist.tasks.LazyTaskColumn
import space.brandoin.focuslist.alerts.NewTaskDialog
import space.brandoin.focuslist.alerts.RenameTaskAlert
import space.brandoin.focuslist.viewmodels.TasksViewModel
import space.brandoin.focuslist.ui.theme.FocusListTheme

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
    var openBreakAlert by rememberSaveable { mutableStateOf(false) }
    var openNameDialog by rememberSaveable { mutableStateOf(false) }
    var openRenameDialog by rememberSaveable { mutableStateOf(false) }
    var currentlyRenaming by rememberSaveable { mutableIntStateOf(-1) }

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
                    Header()
                }
                Surface(
                    modifier = modifier.fillMaxSize(),
                    shape = RoundedCornerShape(28.dp, 28.dp, 0.dp, 0.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    if (openBreakAlert) {
                        BreakAlert({ openBreakAlert = false }, onRequestBreak)
                    }
                    if (openNameDialog) {
                        NewTaskDialog(
                            { openNameDialog = false },
                            tasksAreCompleted, tasksAreNotCompleted
                        )
                    }
                    if (openRenameDialog && currentlyRenaming != -1) {
                        RenameTaskAlert(currentlyRenaming, {
                            currentlyRenaming = -1
                            openRenameDialog = false
                        })
                    }
                    LazyTaskColumn(tasksAreCompleted, tasksAreNotCompleted, { openRenameDialog = true; currentlyRenaming = it })
                    if (viewModel.tasks.isEmpty()) {
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
                    Toolbar(
                        { openBreakAlert = true },
                        {
                            viewModel.clearAll()
                            stopForegroundService()
                        },
                        { onSettingsButtonClick() },
                        { onAppBlockListButtonClick() }
                    )
                    AddTodoButton({
                        openNameDialog = true
                    })
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