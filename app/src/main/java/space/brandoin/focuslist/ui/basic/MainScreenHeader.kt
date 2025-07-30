package space.brandoin.focuslist.ui.basic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.brandoin.focuslist.viewmodels.TasksViewModel
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun Header(
    viewModel: TasksViewModel = viewModel()
) {
    val animatedProgress by animateFloatAsState(
        targetValue = viewModel.percentage,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )
    Row(Modifier.padding(top = 24.dp)) {
        Text(
            "Focus List",
            Modifier.padding(horizontal = 12.dp).weight(1f)
                .align(Alignment.CenterVertically),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.W800,
        )
    }
    Row(Modifier.padding(top = 8.dp)) {
        Progress(viewModel.percentage, animatedProgress)
    }
}

@Composable
fun Progress(
    percentageComplete: Float,
    animatedProgress: Float,
) {
    Text(
        "${(percentageComplete * 100).roundToInt()}%",
        modifier = Modifier.padding(horizontal = 12.dp),
        fontWeight = FontWeight.Bold
    )
    val x = percentageComplete
    LinearWavyProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier.padding(top = 8.dp, bottom = 0.dp, end = 12.dp)
            .fillMaxWidth(),
        waveSpeed = 50.dp * x.pow(3) + 50.dp * x.pow(4) + 40.dp * x.pow(3) + 18.dp
    )
}