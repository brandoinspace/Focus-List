package space.brandoin.focuslist.data

import android.util.Log
import kotlinx.serialization.json.Json
import space.brandoin.focuslist.viewmodels.Task
import java.io.File

private const val TASKS_JSON_FILENAME = "tasks.json"

class TasksJSONStore {
    companion object {
        lateinit var filesDir: File

        fun readTasksJSON(): List<Task> {
            // TODO: size check
            var json = emptyList<Task>()
            val file = File(filesDir, TASKS_JSON_FILENAME)
            taskListFileCheck { json = Json.decodeFromString<List<Task>>(file.readText()) }
            return json
        }

        fun writeTasksJSON(json: String) {
            File(filesDir, TASKS_JSON_FILENAME).writeText(json)
        }

        private fun taskListFileCheck(action: () -> Unit) {
            val file = File(filesDir, TASKS_JSON_FILENAME)
            if (file.exists()) {
                try {
                    action()
                } catch(e: Exception) {
                    Log.d("focus list JSON", e.message ?: "")
                }
            }
        }
    }
}