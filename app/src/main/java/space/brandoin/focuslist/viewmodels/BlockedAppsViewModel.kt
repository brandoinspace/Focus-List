package space.brandoin.focuslist.viewmodels

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import space.brandoin.focuslist.R
import space.brandoin.focuslist.data.GlobalJsonStore
import java.io.ByteArrayOutputStream

@Serializable
data class AppInfo(
    val label: String,
    val packageName: String,
    @Serializable(with = ImageBitmapAsStringSerializer::class)
    var icon: ImageBitmap
)

class BlockedAppsViewModel : ViewModel() {
    private val _blockedApps = emptyList<AppInfo>().toMutableStateList()

    init {
        _blockedApps.clear()
        for (app in GlobalJsonStore.readBlockedAppsJSON()) {
            _blockedApps.add(app)
        }
    }

    fun addBlockedApp(app: AppInfo) {
        _blockedApps.add(app)
        GlobalJsonStore.writeBlockedAppsJSON(toJsonString())
    }

    fun removeBlockedApp(app: AppInfo) {
        val i = packageNameList().indexOfFirst { p ->
            p == app.packageName
        }
        _blockedApps.removeAt(i)
        GlobalJsonStore.writeBlockedAppsJSON(toJsonString())
    }

    fun containsAppInfo(app: AppInfo): Boolean {
        return packageNameList().contains(app.packageName)
    }

    fun toJsonString(): String {
        return Json.encodeToString(_blockedApps.toList())
    }

    fun packageNameList(): List<String> {
        return _blockedApps.map { a ->
            a.packageName
        }
    }

    fun clearAll() {
        _blockedApps.clear()
        GlobalJsonStore.writeBlockedAppsJSON(toJsonString())
    }
}

fun ApplicationInfo.toJSONableAppInfo(pm: PackageManager, width: Int = 200, height: Int = 200): AppInfo {
    return AppInfo(
        pm.getApplicationLabel(this).toString(),
        this.packageName,
        pm.getApplicationIcon(this).toBitmap(width, height).asImageBitmap()
    )
}

// https://stackoverflow.com/a/13563230
object ImageBitmapAsStringSerializer : KSerializer<ImageBitmap> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("focuslist.android.imagebitmap",
        PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: ImageBitmap
    ) {
        val baos = ByteArrayOutputStream()
        val bitmap = value.asAndroidBitmap()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        val temp = Base64.encodeToString(b, Base64.DEFAULT)
        encoder.encodeString(temp)
    }

    override fun deserialize(decoder: Decoder): ImageBitmap {
        try {
            val encodeByte = Base64.decode(decoder.decodeString(), Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
            return bitmap.asImageBitmap()
        } catch (e: Exception) {
            Log.d("focus list decoder", e.message ?: "")
            return R.drawable.ic_launcher_foreground.toDrawable().toBitmap(200, 200).asImageBitmap()
        }
    }

}