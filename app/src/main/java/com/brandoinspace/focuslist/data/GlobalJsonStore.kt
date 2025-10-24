package com.brandoinspace.focuslist.data

import android.util.Log
import com.brandoinspace.focuslist.viewmodels.AppInfo
import kotlinx.serialization.json.Json
import java.io.File

private const val PERCENTAGE_JSON_FILENAME = "completion_percentage.json"
private const val BLOCKED_APPS_JSON_FILENAME = "blocked_apps.json"
private const val SHOULD_BLOCK_ALL_JSON_FILENAME = "should_block_all.json"
private const val IS_FIRST_TIME_JSON_FILENAME = "is_first_time.json"
private const val BREAK_COOLDOWN_TIME_JSON_FILENAME = "break_cooldown_time.json"

class GlobalJsonStore {
    companion object {
        lateinit var filesDir: File

        fun writePercentageJSON(float: Float) {
            Log.d("json percent", float.toString())
            File(filesDir, PERCENTAGE_JSON_FILENAME).writeText(Json.encodeToString(float))
        }

        fun writeBlockedAppsJSON(json: String) {
            File(filesDir, BLOCKED_APPS_JSON_FILENAME).writeText(json)
        }

        fun writeShouldBlockAllJSON(blockAll: Boolean) {
            File(filesDir, SHOULD_BLOCK_ALL_JSON_FILENAME).writeText(Json.encodeToString(blockAll))
        }

        fun writeOpenedBefore() {
            File(filesDir, IS_FIRST_TIME_JSON_FILENAME).writeText(Json.encodeToString(false))
        }

        fun writeCooldownTime(time: Long) {
            File(filesDir, BREAK_COOLDOWN_TIME_JSON_FILENAME).writeText(Json.encodeToString(time))
        }

        fun readShouldBlockAllJSON(): Boolean {
            var block = false
            val file = File(filesDir, SHOULD_BLOCK_ALL_JSON_FILENAME)
            fileCheck(SHOULD_BLOCK_ALL_JSON_FILENAME) {
                block = Json.decodeFromString(file.readText())
            }
            return block
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
            fileCheck(BLOCKED_APPS_JSON_FILENAME) {
                json = Json.decodeFromString<List<AppInfo>>(file.readText())
            }
            return json
        }

        fun readCooldownTime(): Long {
            var long = 0L
            val file = File(filesDir, BREAK_COOLDOWN_TIME_JSON_FILENAME)
            fileCheck(BREAK_COOLDOWN_TIME_JSON_FILENAME) {
                long = Json.decodeFromString(file.readText())
            }
            return long
        }

        fun getBlockedAppPackageNameString(): String {
            val apps = readBlockedAppsJSON()
            val names = apps.map { appInfo -> appInfo.packageName }
            return Json.encodeToString(names)
        }

        fun isFirstTime(): Boolean {
            var isFirstTime = true
            val file = File(filesDir, IS_FIRST_TIME_JSON_FILENAME)
            fileCheck(IS_FIRST_TIME_JSON_FILENAME) {
                isFirstTime = Json.decodeFromString(file.readText())
            }
            Log.d("focus json read", "$isFirstTime")
            return isFirstTime
        }

        private fun fileCheck(child: String, action: () -> Unit) {
            val file = File(filesDir, child)
            if (file.exists()) {
                try {
                    action()
                } catch (e: Exception) {
                    Log.d("focus list JSON", e.message ?: "")
                }
            }
        }
    }
}