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
import kotlinx.coroutines.runBlocking
import study.doomscrolling.app.R
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.prompts.PromptRepository
import study.doomscrolling.app.data.repository.SessionRepository
import study.doomscrolling.app.domain.MonitoredApps
import study.doomscrolling.app.domain.intervention.InterventionCompletionNotifier
import study.doomscrolling.app.domain.intervention.InterventionEngine
import study.doomscrolling.app.domain.prompts.PromptEngine
import study.doomscrolling.app.domain.prompts.PromptManager
import study.doomscrolling.app.domain.prompts.PromptRenderer
import study.doomscrolling.app.domain.models.Session
import study.doomscrolling.app.domain.models.createSession
import study.doomscrolling.app.domain.study.StudyArmManager
import study.doomscrolling.app.ui.MainActivity

/**
 * Foreground service that detects when monitored social media apps enter
 * the foreground and tracks session start/end. Uses UsageStatsManager and
 * UsageEvents (ACTIVITY_RESUMED / ACTIVITY_PAUSED).
 */
class UsageTrackingService : Service() {

    private val detector by lazy { ForegroundAppDetector(this) }
    private val db by lazy { AppDatabase.getInstance(applicationContext) }
    private val repository by lazy { SessionRepository(db.sessionDao(), db.deviceDao()) }
    private val studyArmManager by lazy { StudyArmManager(db.deviceDao()) }
    private val promptRenderer by lazy { PromptRenderer() }
    private val promptRepository by lazy { PromptRepository(applicationContext) }
    private val promptEngine by lazy { PromptEngine(promptRepository, promptRenderer) }
    private val promptManager by lazy { PromptManager(applicationContext) }
    private val interventionEngine by lazy {
        InterventionEngine(
            sessionRepository = repository,
            interventionDao = db.interventionDao(),
            onboardingResponseDao = db.onboardingResponseDao(),
            studyArmManager = studyArmManager,
            promptEngine = promptEngine,
            promptManager = promptManager
        ).also { engine ->
            InterventionCompletionNotifier.listener = { interventionId, sessionId, action ->
                engine.onInterventionCompleted(interventionId, sessionId, action)
            }
        }
    }
    private var currentSession: Session? = null
    private var mismatchCount: Int = 0

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
                    val session = currentSession
                    when {
                        foreground == null -> { /* ignore transient detection */ }
                        foreground == session?.packageName -> {
                            mismatchCount = 0
                            if (MonitoredApps.isMonitored(foreground) && (session == null || session.packageName != foreground)) {
                                endSession()
                                startSession(foreground)
                            }
                        }
                        foreground in SYSTEM_PACKAGES -> { /* ignore system overlays */ }
                        MonitoredApps.isMonitored(foreground) -> {
                            endSession()
                            startSession(foreground)
                            mismatchCount = 0
                        }
                        else -> {
                            mismatchCount++
                            if (mismatchCount >= MISMATCH_THRESHOLD) {
                                endSession()
                                mismatchCount = 0
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Tracking loop error", e)
                }
                Thread.sleep(POLL_INTERVAL_MS)
            }
        }.start()
    }

    private fun startSession(packageName: String) {
        if (repository.isSystemBlocklisted(packageName)) return
        val (sessionId, startTs) = runBlocking { repository.startSessionOrExtendLast(packageName) }
        currentSession = createSession(sessionId, packageName, startTs)
        interventionEngine.startMonitoring(sessionId, packageName, startTs)
        Log.i(TAG, "Session started: ${MonitoredApps.displayName(packageName)}")
    }

    private fun endSession() {
        val session = currentSession ?: return
        interventionEngine.stopMonitoring()
        runBlocking { repository.endSession(session.sessionId) }
        session.end(System.currentTimeMillis())
        Log.i(TAG, "Session ended: duration=${session.durationSeconds}s")
        currentSession = null
    }

    companion object {
        private const val TAG = "UsageTracking"
        private const val CHANNEL_ID = "usage_tracking"
        private const val NOTIFICATION_ID = 1
        private const val POLL_INTERVAL_MS = 2000L
        /** Consecutive polls the foreground must differ from session before ending (avoids activity transition glitches). */
        private const val MISMATCH_THRESHOLD = 3

        /** System/overlay packages to ignore so sessions don't end on permission dialogs, notification shade, etc. Launcher excluded so going home can end session after 3 cycles. */
        private val SYSTEM_PACKAGES = setOf(
            "com.android.systemui",
            "com.google.android.permissioncontroller",
            "com.android.settings"
        )

        const val ACTION_START = "study.doomscrolling.app.START_USAGE_TRACKING"
    }
}
