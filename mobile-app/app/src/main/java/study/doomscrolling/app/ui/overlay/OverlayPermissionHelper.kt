package study.doomscrolling.app.ui.overlay

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Helper for SYSTEM_ALERT_WINDOW (overlay) permission required to show prompts over other apps.
 */
object OverlayPermissionHelper {

    fun hasOverlayPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return Settings.canDrawOverlays(context)
    }

    /**
     * Opens the system overlay permission screen for this app.
     */
    fun requestOverlayPermission(activity: android.app.Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}")
        )
        activity.startActivity(intent)
    }
}
