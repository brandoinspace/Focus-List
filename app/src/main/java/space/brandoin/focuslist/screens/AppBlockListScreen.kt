package space.brandoin.focuslist.screens

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DoorBack
import androidx.compose.material.icons.outlined.MobileFriendly
import androidx.compose.material.icons.outlined.MobileOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TonalToggleButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.brandoin.focuslist.viewmodels.BlockedAppsViewModel
import space.brandoin.focuslist.R
import space.brandoin.focuslist.ui.theme.FocusListTheme
import space.brandoin.focuslist.viewmodels.toJSONableAppInfo

// TODO: Separate this into different composables for clarity
// TODO: make this more material 3 expressive
@SuppressLint("QueryPermissionsNeeded")
@Composable
fun AppBlockList(
    returnToMainScreenClick: (Boolean) -> Unit,
    onBlockedAppListChanges: () -> Unit,
    modifier: Modifier = Modifier,
    blockedAppsViewModel: BlockedAppsViewModel = viewModel()
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            Column(
                modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(top = 36.dp)
            ) {
                Row(Modifier.padding(top = 24.dp)) {
                    Text(
                        "Apps to Block",
                        Modifier.padding(horizontal = 12.dp).weight(1f)
                            .align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W800,
                    )
                    IconToggleButton(
                        checked = false,
                        onCheckedChange = { /* TODO */ }
                    ) {
                        Icon(Icons.Outlined.MobileOff, "Block All Apps")
                    }
                    IconToggleButton(
                        checked = false,
                        onCheckedChange = { blockedAppsViewModel.clearAll() }
                    ) {
                        Icon(Icons.Outlined.MobileFriendly, "Unblock All Apps")
                    }
                    IconToggleButton(
                        checked = false,
                        onCheckedChange = { clicked -> returnToMainScreenClick(clicked) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Outlined.DoorBack, "Back to Focus List")
                    }
                }
                Row(Modifier.padding(top = 8.dp)) {
                    Text(
                        "Select which apps will be blocked if your task list is incomplete:",
                        Modifier.padding(horizontal = 12.dp).weight(1f)
                            .align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Surface(
                    modifier = modifier.fillMaxSize().padding(top = 8.dp),
                    shape = RoundedCornerShape(28.dp, 28.dp, 0.dp, 0.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    val pm = LocalContext.current.packageManager
                    var packages = pm.getInstalledApplications(
                        PackageManager.GET_META_DATA
                    ).filter { p ->
                        (!p.packageName.contains("focuslist")) && (isExceptionApp(p.packageName) || ((p.flags and ApplicationInfo.FLAG_SYSTEM) == 0))
                    }
                    packages =
                        packages.sortedByDescending { p -> pm.getApplicationLabel(p).toString() }
                    packages = packages.asReversed()
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(200.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        items(
                            packages,
                            key = { p -> p.packageName }
                        ) { pkg ->
                            val p = pkg.toJSONableAppInfo(pm)
                            val background = if (!blockedAppsViewModel.containsAppInfo(p)) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            }
                            val border = if (!blockedAppsViewModel.containsAppInfo(p)) {
                                null
                            } else {
                                BorderStroke(
                                    1.dp,
                                    SolidColor(MaterialTheme.colorScheme.outlineVariant)
                                )
                            }
                            val textColor = if (!blockedAppsViewModel.containsAppInfo(p)) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            val imageShape = if (!blockedAppsViewModel.containsAppInfo(p)) {
                                MaterialShapes.Circle
                            } else {
                                MaterialShapes.SoftBurst
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(8.dp)
                            ) {
                                Surface(
                                    shape = MaterialShapes.Square.toShape(),
                                    color = background,
                                    tonalElevation = 20.dp,
                                    border = border
                                ) {
                                    val label = p.label
                                    Column(Modifier.fillMaxHeight().padding(top = 8.dp)) {
                                        Image(
                                            bitmap = p.icon,
                                            label,
                                            modifier.align(Alignment.CenterHorizontally)
                                                .clip(imageShape.toShape())
                                        )
                                        Text(
                                            label,
                                            modifier.fillMaxWidth().padding(top = 4.dp),
                                            textAlign = TextAlign.Center,
                                            color = textColor
                                        )
                                        Row(
                                            Modifier.align(Alignment.CenterHorizontally)
                                                .fillMaxHeight()
                                        ) {
                                            TonalToggleButton(
                                                checked = blockedAppsViewModel.containsAppInfo(p),
                                                onCheckedChange = {
                                                    if (!blockedAppsViewModel.containsAppInfo(p)
                                                    ) {
                                                        blockedAppsViewModel.addBlockedApp(p)
                                                        onBlockedAppListChanges()
                                                    } else {
                                                        blockedAppsViewModel.removeBlockedApp(p)
                                                        onBlockedAppListChanges()
                                                    }
                                                },
                                                modifier = Modifier.align(Alignment.Bottom)
                                                    .padding(bottom = 8.dp)
                                            ) {
                                                if (!blockedAppsViewModel.containsAppInfo(p)) {
                                                    Text("Press to Block")
                                                } else {
                                                    Text("Blocked")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isExceptionApp(name: String): Boolean {
    val exceptions = arrayOf(
        "com.google.android.apps.youtube.music",
        "com.google.android.youtube",
        "com.google.android.apps.translate",
        "com.google.android.apps.pixel.health",
        "com.google.android.apps.tasks",
        "com.google.android.apps.docs.editors.slides",
        "com.shazam.android",
        "com.android.settings",
        "com.google.android.apps.recorder",
        "com.google.android.apps.weather",
        "com.google.android.apps.pixel.relationships", // vip
        "com.google.android.apps.tips",
        "com.google.android.apps.pixel.creativeassistant",
        "com.google.android.apps.pixel.agent", // screenshots
        "com.google.android.apps.photos",
        "com.google.android.dialer",
        "com.google.android.apps.messaging",
        "com.google.android.apps.tachyon", // meet
        "com.google.android.apps.maps",
        "com.google.android.keep",
        "com.google.android.apps.walletnfcrel",
        "com.google.android.videos", // tv
        "com.android.vending", // play store
        "com.google.android.apps.books",
        "com.google.android.apps.subscriptions.red", // google 1
        "com.google.android.apps.magazines", // news
        "com.google.android.googlequicksearchbox", // google app
        "com.google.android.gm", //gmail
        "com.google.android.apps.docs", //drive
        "com.google.android.apps.docs.editors.docs",
        "com.google.android.contacts",
        "com.google.android.apps.classroom",
        "com.google.android.deskclock", //clock
        "com.android.chrome",
        "com.google.android.GoogleCamera",
        "com.google.android.calendar",
        "com.google.android.calculator",
    )
    return exceptions.contains(name)
}

@Preview
@Composable
fun AppBlockListPreview() {
    FocusListTheme {
        AppBlockList({}, {})
    }
}