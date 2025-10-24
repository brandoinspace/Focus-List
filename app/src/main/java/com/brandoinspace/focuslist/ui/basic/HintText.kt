package com.brandoinspace.focuslist.ui.basic

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HintText(modifier: Modifier = Modifier) {
    Row {
        Text(
            "Press the + Button To\nCreate a New Task",
            modifier = modifier
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .padding(bottom = 80.dp)
                .align(Alignment.CenterVertically)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}