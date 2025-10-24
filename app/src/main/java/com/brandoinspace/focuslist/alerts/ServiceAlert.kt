package com.brandoinspace.focuslist.alerts

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

@Composable
fun ServiceAlert(
    dismiss: () -> Unit,
    startService: () -> Unit,
) {
    val current = LocalContext.current
    BasicAlertDialog(
        onDismissRequest = dismiss
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Icon(
                    Icons.Rounded.Warning,
                    "Service not Running",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(24.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Service Not Running",
                    Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "The blocking service is currently not running."
                        + " To run the service and allow apps to be blocked, please press 'Start Service' below."
                        + " If the button is disabled, it means you have not enabled the proper permissions to start the service.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(Modifier.align(Alignment.End)) {
                    TextButton(
                        onClick = dismiss,
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        enabled = ActivityCompat.checkSelfPermission(current, Manifest.permission.POST_NOTIFICATIONS)
                                == PackageManager.PERMISSION_GRANTED,
                        onClick = startService,
                    ) {
                        Text("Start Service")
                    }
                }
            }
        }
    }
}