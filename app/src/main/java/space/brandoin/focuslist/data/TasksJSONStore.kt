package space.brandoin.focuslist.data

import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import space.brandoin.focuslist.viewmodels.Task
import java.io.File

private const val TASKS_JSON_FILENAME = "tasks.json"
private const val PERCENTAGE_JSON_FILENAME = "completion_percentage.json"

class TasksJSONStore {
    companion object {
        lateinit var filesDir: File

        fun readTasksJSON(): List<Task> {
            // TODO: size check
            var json = emptyList<Task>()
            val file = File(filesDir, TASKS_JSON_FILENAME)
            fileCheck(TASKS_JSON_FILENAME) { json = Json.decodeFromString(file.readText()) }
            return json
        }

        fun writeTasksJSON(json: String) {
            File(filesDir, TASKS_JSON_FILENAME).writeText(json)
        }

        fun writePercentageJSON(float: Float) {
            File(filesDir, PERCENTAGE_JSON_FILENAME).writeText(Json.encodeToString(float))
        }

        fun readPercentageJSON(): Float {
            var float = 0.0f
            val file = File(filesDir, PERCENTAGE_JSON_FILENAME)
            fileCheck(PERCENTAGE_JSON_FILENAME) { float = Json.decodeFromString(file.readText()) }
            return float
        }

        private fun fileCheck(child: String, action: () -> Unit) {
            val file = File(filesDir, child)
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