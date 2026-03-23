package study.doomscrolling.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.runBlocking
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.domain.study.StudyWindow
import study.doomscrolling.app.services.ForegroundAppDetector
import study.doomscrolling.app.services.UsageTrackingService

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        if (!ForegroundAppDetector.hasUsageStatsPermission(context)) {
            Log.i(TAG, "Boot receiver: usage access not granted; not starting tracking")
            return
        }

        val enrolledAt = runBlocking {
            AppDatabase.getInstance(context.applicationContext).deviceDao().getDevice()?.enrolledAt
        }

        if (StudyWindow.isStudyCompleted(enrolledAt)) {
            Log.i(TAG, "Boot receiver: study completed; not starting tracking")
            return
        }

        try {
            UsageTrackingService.startOrRefresh(context)
            Log.i(TAG, "Boot receiver: tracking service start requested")
        } catch (e: Exception) {
            Log.e(TAG, "Boot receiver: failed to start tracking service", e)
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
