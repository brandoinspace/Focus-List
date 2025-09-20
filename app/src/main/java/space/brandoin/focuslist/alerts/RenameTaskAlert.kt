package space.brandoin.focuslist.alerts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import space.brandoin.focuslist.viewmodels.TasksViewModel

@Composable
fun RenameTaskAlert(
    taskId: Int,
    onNameChanged: () -> Unit,
    viewModel: TasksViewModel = viewModel()
) {
    val task = viewModel.findTask(taskId)
    var nameEntered by remember { mutableStateOf(task.name) }
    // https://stackoverflow.com/a/77573293
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(
        text = nameEntered,
        selection = TextRange(nameEntered.length)
    )) }
    val focusRequester = remember { FocusRequester() }

    BasicAlertDialog(
        onDismissRequest = {
            onNameChanged()
            nameEntered = task.name
        }
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    "Change Name",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(24.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Change Name",
                    Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(16.dp))
                TextField(
                    value = textFieldValueState,
                    onValueChange = { textFieldValueState = it },
                    enabled = true,
                    label = { Text("Name") },
                    supportingText = {
                        Row {
                            Text("${nameEntered.length}/50", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        }
                    },
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Spacer(Modifier.height(24.dp))
                Row(Modifier.align(Alignment.End)) {
                    TextButton(
                        onClick = {
                            onNameChanged()
                            nameEntered = textFieldValueState.text
                        },
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            viewModel.changeTaskName(task, textFieldValueState.text.trim())
                            onNameChanged()
                            nameEntered = textFieldValueState.text
                            if (viewModel.areAllTasksCompleted()) {
                                onNameChanged()
                            } else {
                                onNameChanged()
                            }
                        },
                        enabled = nameEntered.length <= 50 && !nameEntered.isEmpty()
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }

    // https://stackoverflow.com/a/69113298
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
}