package space.brandoin.focuslist.data

import android.util.Log
import kotlinx.serialization.json.Json
import space.brandoin.focuslist.viewmodels.AppInfo
import space.brandoin.focuslist.viewmodels.Task
import java.io.File
import kotlin.collections.List

private const val TASKS_JSON_FILENAME = "tasks.json"
private const val PERCENTAGE_JSON_FILENAME = "completion_percentage.json"
private const val BLOCKED_APPS_JSON_FILENAME = "blocked_apps.json"

class GlobalJsonStore {
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

        fun readBlockedAppsJSON(): List<AppInfo> {
            // TODO: size check
            var json = emptyList<AppInfo>()
            val file = File(filesDir, BLOCKED_APPS_JSON_FILENAME)
            fileCheck(BLOCKED_APPS_JSON_FILENAME) { json = Json.decodeFromString<List<AppInfo>>(file.readText()) }
            return json
        }

        fun writeBlockedAppsJSON(json: String) {
            File(filesDir, BLOCKED_APPS_JSON_FILENAME).writeText(json)
        }

        fun getBlockedAppPackageNameString(): String {
            val apps = readBlockedAppsJSON()
            val names = apps.map { appInfo -> appInfo.packageName }
            return Json.encodeToString(names)
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