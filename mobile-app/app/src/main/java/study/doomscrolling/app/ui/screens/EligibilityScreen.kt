package study.doomscrolling.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import study.doomscrolling.app.services.ForegroundAppDetector

@Composable
fun EligibilityScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var hasUsagePermission by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember { mutableStateOf(hasOverlayPermission(context)) }

    LaunchedEffect(Unit) {
        hasUsagePermission = ForegroundAppDetector.hasUsageStatsPermission(context)
        hasOverlayPermission = hasOverlayPermission(context)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Permissions",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To participate in this study, the app needs:\n\n" +
                "- Usage access permission (to reconstruct app sessions)\n" +
                "- Display over other apps permission (to show brief intervention overlays)",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Usage access permission: " + if (hasUsagePermission) "Granted" else "Not granted",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                openUsageAccessSettings(context)
            }
        ) {
            Text("Open usage access settings")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Display over other apps: " + if (hasOverlayPermission) "Granted" else "Not granted",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                openOverlaySettings(context)
            }
        ) {
            Text("Open overlay settings")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                hasUsagePermission = ForegroundAppDetector.hasUsageStatsPermission(context)
                hasOverlayPermission = hasOverlayPermission(context)
            }
        ) {
            Text("Refresh status")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            enabled = hasUsagePermission && hasOverlayPermission,
            onClick = {
                // Re-check before continuing in case user granted permissions while returning.
                hasUsagePermission = ForegroundAppDetector.hasUsageStatsPermission(context)
                hasOverlayPermission = hasOverlayPermission(context)
                if (hasUsagePermission && hasOverlayPermission) {
                    onComplete()
                }
            }
        ) {
            Text("Continue")
        }
    }
}

private fun hasOverlayPermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true
    }
}

private fun openUsageAccessSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

private fun openOverlaySettings(context: android.content.Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
