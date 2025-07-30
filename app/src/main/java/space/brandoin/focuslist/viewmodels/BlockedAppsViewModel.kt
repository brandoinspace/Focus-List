package space.brandoin.focuslist.viewmodels

import android.content.pm.ApplicationInfo
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import space.brandoin.focuslist.data.GlobalDataStore

class BlockedAppsViewModel(
    private val globalDataStore: GlobalDataStore
) : ViewModel() {
    private val _blockedApps = emptyList<ApplicationInfo>().toMutableStateList()
    val blockedApps: List<ApplicationInfo>
        get() = _blockedApps

    fun addBlockedApp(app: ApplicationInfo) {
        _blockedApps.add(app)
    }

    fun removeBlockedApp(app: ApplicationInfo) {
        _blockedApps.remove(app)
    }

    fun containsApplicationInfo(app: ApplicationInfo): Boolean {
        return _blockedApps.contains(app)
    }
}