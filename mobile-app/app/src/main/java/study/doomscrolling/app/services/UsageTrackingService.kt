package study.doomscrolling.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import study.doomscrolling.app.R
import study.doomscrolling.app.domain.MonitoredApps
import study.doomscrolling.app.domain.models.Session
import study.doomscrolling.app.domain.models.createSession
import study.doomscrolling.app.ui.MainActivity

/**
 * Foreground service that detects when monitored social media apps enter
 * the foreground and tracks session start/end. Uses UsageStatsManager and
 * UsageEvents (ACTIVITY_RESUMED / ACTIVITY_PAUSED).
 */
class UsageTrackingService : Service() {

    private val detector by lazy { ForegroundAppDetector(this) }
    private var currentSession: Session? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!ForegroundAppDetector.hasUsageStatsPermission(this)) {
            Log.w(TAG, "Usage stats permission not granted; session tracking disabled.")
            stopSelf()
            return START_NOT_STICKY
        }
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        startTrackingLoop()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_usage_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply { setShowBadge(false) }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val pending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_usage_title))
            .setContentText(getString(R.string.notification_usage_text))
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    private fun startTrackingLoop() {
        Thread {
            while (true) {
                try {
                    if (!ForegroundAppDetector.hasUsageStatsPermission(this)) break
                    val foreground = detector.getCurrentForegroundPackage()
                    if (MonitoredApps.isMonitored(foreground)) {
                        val pkg = foreground!!
                        if (currentSession == null || currentSession!!.packageName != pkg) {
                            endSession()
                            startSession(pkg)
                        }
                    } else {
                        if (currentSession != null) endSession()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Tracking loop error", e)
                }
                Thread.sleep(POLL_INTERVAL_MS)
            }
        }.start()
    }

    private fun startSession(packageName: String) {
        val now = System.currentTimeMillis()
        currentSession = createSession(packageName, now)
        Log.i(TAG, "Session started: ${MonitoredApps.displayName(packageName)}")
    }

    private fun endSession() {
        val session = currentSession ?: return
        session.end(System.currentTimeMillis())
        Log.i(TAG, "Session ended: duration=${session.durationSeconds}s")
        currentSession = null
    }

    companion object {
        private const val TAG = "UsageTracking"
        private const val CHANNEL_ID = "usage_tracking"
        private const val NOTIFICATION_ID = 1
        private const val POLL_INTERVAL_MS = 2000L

        const val ACTION_START = "study.doomscrolling.app.START_USAGE_TRACKING"
    }
}
