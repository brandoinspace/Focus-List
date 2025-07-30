package space.brandoin.focuslist.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel

class Task(val id: Int, val name: String, initialCompleted: Boolean = false) {
    var completed by mutableStateOf(initialCompleted)
}

class TasksViewModel : ViewModel() {
    private val _tasks = emptyList<Task>().toMutableStateList()
    private var _lastId: Int = 0
    val tasks: List<Task>
        get() = _tasks
    val percentage: Float
        get() = updateProgress()

    fun add(name: String) {
        _tasks.add(Task(_lastId++, name, false))
        updateProgress()
    }

    fun updateProgress(): Float {
        val size = _tasks.size
        val countCompleted = _tasks.count{ it.completed }

        return if (_tasks.isEmpty() || ((size == 1) && countCompleted == 1)) {
            1f
        } else if (size == 1) {
            return 0f
        } else {
            return (countCompleted.toFloat() / size)
        }
    }

    fun clearAll() {
        _tasks.clear()
        updateProgress()
    }

    fun completeTask(item: Task, completed: Boolean = true) {
        _tasks.find { it.id == item.id }?.let { task ->
            task.completed = completed
        }
    }

    fun removeTask(item: Task) {
        _tasks.remove(item)
        updateProgress()
    }

    fun areAllTasksCompleted(): Boolean {
        return _tasks.count{ it.completed } == _tasks.count()
    }
}