package space.brandoin.focuslist.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BlockedAppsData(val apps: List<String>)

// TODO: Error handling
// https://medium.com/@rowaido.game/persistent-data-storage-using-datastore-preferences-in-jetpack-compose-90c481bfed12
class GlobalDataStore(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val BLOCKED_APPS_JSON = stringPreferencesKey("blocked_apps_json")
    }

    val json = Json { ignoreUnknownKeys = true }

    val blockedAppsJson: Flow<List<String>> =
        dataStore.data.map { preferences ->
             json.decodeFromString<BlockedAppsData>(preferences[BLOCKED_APPS_JSON] ?: "{}").apps
        }

    suspend fun saveBlockedAppsJson(blockedApps: List<String>) {
        dataStore.edit { preferences ->
            preferences[BLOCKED_APPS_JSON] = json.encodeToString(BlockedAppsData(blockedApps))
        }
    }
}