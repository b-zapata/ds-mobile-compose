package study.doomscrolling.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import study.doomscrolling.app.services.ForegroundAppDetector

@Composable
fun PermissionsScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var hasUsagePermission by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        hasUsagePermission = ForegroundAppDetector.hasUsageStatsPermission(context)
        hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Permissions",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To participate in this study, the app needs these three permissions:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        PermissionStatusItem(
            label = "Usage access",
            isGranted = hasUsagePermission,
            onOpenSettings = { openUsageAccessSettings(context) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionStatusItem(
            label = "Display over other apps",
            isGranted = hasOverlayPermission,
            onOpenSettings = { openOverlaySettings(context) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionStatusItem(
            label = "Notifications",
            isGranted = hasNotificationPermission,
            onOpenSettings = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                hasUsagePermission = ForegroundAppDetector.hasUsageStatsPermission(context)
                hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
                hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
            }
        ) {
            Text("Refresh status")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            enabled = hasUsagePermission && hasOverlayPermission && hasNotificationPermission,
            onClick = { onComplete() }
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun PermissionStatusItem(
    label: String,
    isGranted: Boolean,
    onOpenSettings: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$label: " + if (isGranted) "Granted" else "Not granted",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onOpenSettings,
            enabled = !isGranted
        ) {
            Text(if (isGranted) "Permission Granted" else "Grant Permission")
        }
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
