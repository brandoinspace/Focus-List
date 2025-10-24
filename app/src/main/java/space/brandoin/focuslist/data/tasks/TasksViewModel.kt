package space.brandoin.focuslist.data.tasks

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import space.brandoin.focuslist.data.GlobalJsonStore

class TasksViewModel(private val tasksRepository: TasksRepository) : ViewModel() {
    val tasks: StateFlow<TasksWrapper> = tasksRepository.getAllTasksStream().map {
        TasksWrapper(it.toMutableStateList())
    }.onEach {
        GlobalJsonStore.writePercentageJSON(getPercentage(it.tasks))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        TasksWrapper()
    )

    suspend fun saveTask(task: TaskEntity) {
        val updated: List<TaskEntity> = tasks.value.tasks.map {
            it.copy(listOrder = it.listOrder + 1)
        }
        tasksRepository.updateAllTasks(updated)
        // Insert task as listOrder 0
        tasksRepository.insertTask(task)
    }

    suspend fun removeTask(task: TaskEntity) {
        val old = task.listOrder
        tasksRepository.deleteTask(task)
        updateTaskOrder(old)
    }

    suspend fun updateCompletion(task: TaskEntity, completed: Boolean) {
        tasksRepository.updateTask(task.copy(completed = completed))
    }

    suspend fun allTasksCompleted(): Boolean {
        return tasksRepository.numberOfIncompleteTasks() == 0
    }


    suspend fun dropTasks() {
        tasksRepository.dropTasks()
    }

    suspend fun updateTaskOrder(oldIndex: Int) {
        val updated = tasks.value.tasks
        for (t in updated) {
            if (t.listOrder > oldIndex) {
                t.listOrder--
            }
        }
        tasksRepository.updateAllTasks(updated)
    }

    suspend fun updateAllTasks(updated: List<TaskEntity>) {
        tasksRepository.updateAllTasks(updated)
    }

    suspend fun updateTask(task: TaskEntity) {
        tasksRepository.updateTask(task)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class TasksWrapper(
    var tasks: SnapshotStateList<TaskEntity> = mutableStateListOf()
)

fun getPercentage(t: List<TaskEntity>): Float {
    val size = t.size
    val completed = t.count { it.completed }
    val prog = if (t.isEmpty() || ((size == 1) && completed == 1)) {
        1f
    } else if (size == 1) {
        0f
    } else {
        (completed.toFloat() / size)
    }
    return prog
}