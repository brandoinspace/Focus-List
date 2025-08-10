package space.brandoin.focuslist.data

import android.util.Log
import kotlinx.serialization.json.Json
import space.brandoin.focuslist.viewmodels.AppInfo
import java.io.File

private const val BLOCKED_APPS_JSON_FILENAME = "blocked_apps.json"

class BlockedAppsJSONStore {
    companion object {
        lateinit var filesDir: File
        fun readBlockedAppsJSON(): List<AppInfo> {
            // TODO: size check
            var json = emptyList<AppInfo>()
            val file = File(filesDir, BLOCKED_APPS_JSON_FILENAME)
            blockListFileCheck { json = Json.decodeFromString<List<AppInfo>>(file.readText()) }
            return json
        }

        fun writeBlockedAppsJSON(json: String) {
            File(filesDir, BLOCKED_APPS_JSON_FILENAME).writeText(json)
        }

        fun getBlockedAppsString(): String {
            var text = ""
            val file = File(filesDir, BLOCKED_APPS_JSON_FILENAME)
            blockListFileCheck { text = file.readText() }
            return text
        }

        fun decodeToBlockedAppsList(json: String): List<AppInfo> {
            return Json.decodeFromString<List<AppInfo>>(json)
        }

        private fun blockListFileCheck(action: () -> Unit) {
            val file = File(filesDir, BLOCKED_APPS_JSON_FILENAME)
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