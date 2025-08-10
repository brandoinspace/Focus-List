package space.brandoin.focuslist.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import space.brandoin.focuslist.ui.theme.FocusListTheme
import kotlin.math.pow

@Composable
fun BlockedScreen(
    onButtonOpenApp: () -> Unit,
    onButtonTakeBreak: () -> Unit,
    completionPercentage: Float,
) {
    Surface {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .alpha(0.95f),
            contentAlignment = Alignment.Center,
        ) {
            Column(Modifier.padding(bottom = 20.dp)) {
                Row(Modifier.align(Alignment.CenterHorizontally)) {
                    Surface(
                        Modifier.size(100.dp),
                        shape = MaterialShapes.Cookie9Sided.toShape(),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            Icons.Filled.FrontHand,
                            "Apps are Blocked",
                            modifier = Modifier.padding(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Row(Modifier.align(Alignment.CenterHorizontally)) {
                    Text(
                        "This App is Blocked",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 24.dp)) {
                    Text(
                        "Finish your tasks to unblock this app or take a break.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)) {
                    Button(
                        onClick = onButtonOpenApp,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text("Open FocusList")
                    }
                    Button(
                        onClick = onButtonTakeBreak,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text("Take a Break")
                    }
                }
                // TODO: get percentage data
                Row(Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp, start = 16.dp, end = 16.dp)) {
                    val x = completionPercentage
                    LinearWavyProgressIndicator(
                        progress = { x },
                        modifier = Modifier.padding(top = 8.dp, bottom = 0.dp, end = 12.dp)
                            .fillMaxWidth(),
                        waveSpeed = 50.dp * x.pow(3) + 50.dp * x.pow(4) + 40.dp * x.pow(3) + 18.dp
                    )
                }
            }
            Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 45.dp)) {
                Text("Navigation is Still Available", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BlockedScreenPreview() {
    FocusListTheme {
        BlockedScreen({}, {}, 0.75f)
    }
}