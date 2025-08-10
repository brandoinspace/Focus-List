package space.brandoin.focuslist.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import space.brandoin.focuslist.data.TasksJSONStore

@Serializable(with = TaskAsStringSerializer::class)
class Task(val id: Int = 0, val name: String = "My Task", completed: Boolean = false) {
    var completed by mutableStateOf(completed)
}

class TasksViewModel : ViewModel() {
    private val _tasks = emptyList<Task>().toMutableStateList()
    val tasks: List<Task>
        get() = _tasks
    val percentage: Float
        get() = updateProgress()
    private var _lastId: Int = 0

    init {
        _tasks.clear()
        var id = 0
        for (task in TasksJSONStore.readTasksJSON()) {
            _tasks.add(task)
            if (task.id > id) {
                id = task.id
            }
        }
        _lastId = ++id
        updateProgress()
    }

    fun add(name: String) {
        _tasks.add(Task(_lastId++, name, false))
        updateProgress()
        TasksJSONStore.writeTasksJSON(Json.encodeToString(_tasks.toList()))
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
        TasksJSONStore.writeTasksJSON(Json.encodeToString(_tasks.toList()))
    }

    fun completeTask(item: Task, completed: Boolean = true) {
        _tasks.find { it.id == item.id }?.let { task ->
            task.completed = completed
        }
        TasksJSONStore.writeTasksJSON(Json.encodeToString(_tasks.toList()))
    }

    fun removeTask(item: Task) {
        _tasks.remove(item)
        updateProgress()
        TasksJSONStore.writeTasksJSON(Json.encodeToString(_tasks.toList()))
    }

    fun areAllTasksCompleted(): Boolean {
        return _tasks.count{ it.completed } == _tasks.count()
    }
}

@Serializable
data class TaskTemp(val id: Int, val name: String, var completed: Boolean = false)

object TaskAsStringSerializer : KSerializer<Task?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("focuslist.task",
        PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Task?
    ) {
        val temp = TaskTemp(value!!.id, value.name, value.completed)
        val json = Json.encodeToString(temp)
        encoder.encodeString(json)
    }

    override fun deserialize(decoder: Decoder): Task? {
        var task: Task? = null
        try {
            val jsonElement = Json.parseToJsonElement(decoder.decodeString())
            var id = 0
            var name = "My Task"
            var completed = false
            // TODO: handle this with dialogue to user
            try {
                id = Json.decodeFromJsonElement<Int>(jsonElement.jsonObject["id"]!!)
            } catch(e: Exception) {
                Log.d("focus list decoder", e.message ?: "")
            }
            try {
                name = Json.decodeFromJsonElement<String>(jsonElement.jsonObject["name"]!!)
            } catch(e: Exception) {
                Log.d("focus list decoder", e.message ?: "")
            }
            try {
                completed = Json.decodeFromJsonElement<Boolean>(jsonElement.jsonObject["completed"]!!)
            } catch(e: Exception) {
                Log.d("focus list decoder", e.message ?: "")
            }
            task = Task(id, name, completed)
        } catch (e: Exception) {
            Log.d("focus list decoder", e.message ?: "")
        }
        return task
    }
}