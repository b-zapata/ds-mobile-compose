package study.doomscrolling.app.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import study.doomscrolling.app.BuildConfig
import study.doomscrolling.app.data.dao.DeviceDao
import study.doomscrolling.app.data.dao.SessionDao
import study.doomscrolling.app.data.entities.SessionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

data class AppUsageStat(
    val appPackage: String,
    val totalSeconds: Long
)

data class DailySessionStat(
    val date: String,
    val sessionCount: Int
)

data class BaselineStats(
    val totalSessions: Int,
    val totalDurationSeconds: Long,
    val avgSessionSeconds: Double,
    val usageByApp: List<AppUsageStat>,
    val sessionsPerDay: List<DailySessionStat>,
    val usageByAppByDay: Map<String, List<AppUsageStat>>
)

/** Single threshold: gap for merging same-app sessions and minimum session duration (ms). */
private const val MERGE_GAP_MS = 2000L
private const val MIN_SESSION_DURATION_MS = 2000L

/** Lookback before day start when using event-based baseline (captures segments spanning midnight). */
private const val BASELINE_EVENT_LOOKBACK_MS = 3L * 60L * 60L * 1000L

/** Package names excluded from session tracking (launcher, system UI). */
private val SYSTEM_PACKAGE_BLOCKLIST: Set<String> = setOf(
    "com.google.android.apps.nexuslauncher",
    "com.android.launcher",
    "com.android.launcher2",
    "com.android.launcher3",
    "com.android.systemui"
)

/**
 * Option B: INSERT on session start, UPDATE on session end.
 * Same-app sessions within MERGE_GAP_MS are merged to reduce micro-session fragmentation.
 */
class SessionRepository(
    private val sessionDao: SessionDao,
    private val deviceDao: DeviceDao
) {

    /** True if the package is launcher/system and should not be tracked. */
    fun isSystemBlocklisted(packageName: String): Boolean = packageName in SYSTEM_PACKAGE_BLOCKLIST

    /**
     * Start a new session or extend the most recent ended session for this package
     * if it ended within [MERGE_GAP_MS]. Returns (sessionId, startTimestampMs) for milestone use.
     */
    suspend fun startSessionOrExtendLast(packageName: String): Pair<String, Long> {
        val deviceId = requireDeviceId()
        val now = System.currentTimeMillis()
        val mergeCutoff = now - MERGE_GAP_MS
        val last = sessionDao.getMostRecentEndedSession(deviceId, packageName, mergeCutoff)
        if (last != null) {
            val durationSeconds = ((now - last.startTimestamp) / 1000).coerceAtLeast(0L)
            val updated = last.copy(
                endTimestamp = now,
                durationSeconds = durationSeconds
            )
            sessionDao.updateSession(updated)
            Log.i(TAG, "Session extended (merge): ${last.sessionId}")
            return last.sessionId to last.startTimestamp
        }
        val sessionId = UUID.randomUUID().toString()
        val entity = SessionEntity(
            sessionId = sessionId,
            deviceId = deviceId,
            packageName = packageName,
            startTimestamp = now,
            endTimestamp = null,
            durationSeconds = null,
            createdAt = now
        )
        sessionDao.insertSession(entity)
        Log.i(TAG, "DB session inserted")
        return sessionId to now
    }

    /**
     * Generate sessionId, insert row, return sessionId.
     * Prefer [startSessionOrExtendLast] for live tracking to apply merge rule.
     */
    suspend fun startSession(packageName: String): String =
        startSessionOrExtendLast(packageName).first

    /**
     * Returns the session if it exists and is still active (endTimestamp == null).
     */
    suspend fun getActiveSession(sessionId: String): SessionEntity? =
        sessionDao.getActiveSession(sessionId)?.takeIf { it.endTimestamp == null }

    /**
     * Update endTimestamp and durationSeconds for the given session.
     * Sessions shorter than MIN_SESSION_DURATION_MS are deleted (not persisted) so baseline and live use the same rules.
     */
    suspend fun endSession(sessionId: String) {
        val session = sessionDao.getActiveSession(sessionId) ?: return
        val now = System.currentTimeMillis()
        val durationMs = now - session.startTimestamp
        val durationSeconds = (durationMs / 1000).coerceAtLeast(0L)
        if (durationMs < MIN_SESSION_DURATION_MS) {
            sessionDao.deleteSessions(listOf(sessionId))
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Dropped short session ${sessionId.take(8)}... (${durationMs}ms < ${MIN_SESSION_DURATION_MS}ms)")
            }
            return
        }
        val updated = session.copy(
            endTimestamp = now,
            durationSeconds = durationSeconds
        )
        sessionDao.updateSession(updated)
        Log.i(TAG, "DB session updated")
    }

    /**
     * Compute baseline statistics for the last 7 days using the sessions table.
     */
    suspend fun getBaselineStats(): BaselineStats {
        val now = System.currentTimeMillis()
        val sevenDaysMs = 7L * 24L * 60L * 60L * 1000L
        val since = now - sevenDaysMs
        val allSessions = sessionDao.getSessions()
        val recent = allSessions.filter { it.startTimestamp >= since }

        val totalSessions = recent.size
        // Use stored duration when available; otherwise approximate from timestamps.
        fun effectiveDurationSeconds(session: SessionEntity): Long {
            val stored = session.durationSeconds
            if (stored != null) return stored
            val end = session.endTimestamp ?: return 0L
            return ((end - session.startTimestamp) / 1000).coerceAtLeast(0L)
        }

        val totalDurationSeconds = recent.sumOf { effectiveDurationSeconds(it) }
        val avgSessionSeconds = if (recent.isNotEmpty()) {
            totalDurationSeconds.toDouble() / recent.size.toDouble()
        } else {
            0.0
        }

        val zoneId = ZoneId.systemDefault()
        val dayFormatter = DateTimeFormatter.ofPattern("EEE") // Mon, Tue, ...
        val sessionsByDay: Map<String, List<SessionEntity>> = recent.groupBy { entity ->
            val instant = Instant.ofEpochMilli(entity.startTimestamp)
            dayFormatter.format(instant.atZone(zoneId).toLocalDate())
        }

        val usageByApp = recent
            .groupBy { it.packageName }
            .map { (pkg, sessions) ->
                AppUsageStat(
                    appPackage = pkg,
                    totalSeconds = sessions.sumOf { effectiveDurationSeconds(it) }
                )
            }
            .sortedByDescending { it.totalSeconds }

        val sessionsPerDay = sessionsByDay
            .map { (day, sessions) ->
                DailySessionStat(
                    date = day,
                    sessionCount = sessions.size
                )
            }

        val usageByAppByDay: Map<String, List<AppUsageStat>> = sessionsByDay
            .mapValues { (_, daySessions) ->
                daySessions
                    .groupBy { it.packageName }
                    .map { (pkg, sessions) ->
                        AppUsageStat(
                            appPackage = pkg,
                            totalSeconds = sessions.sumOf { effectiveDurationSeconds(it) }
                        )
                    }
                    .sortedByDescending { it.totalSeconds }
            }

        return BaselineStats(
            totalSessions = totalSessions,
            totalDurationSeconds = totalDurationSeconds,
            avgSessionSeconds = avgSessionSeconds,
            usageByApp = usageByApp,
            sessionsPerDay = sessionsPerDay,
            usageByAppByDay = usageByAppByDay
        )
    }

    /**
     * Event-based screen usage for one interval (Mindful-style). Pair by package+className,
     * clip to [startMs, endMs], sum per package. Used for baseline import.
     */
    private fun getScreenUsageForInterval(
        usageStatsManager: UsageStatsManager,
        startMs: Long,
        endMs: Long
    ): Map<String, Long> {
        val usageMapMs = mutableMapOf<String, Long>()
        data class InProgress(val packageName: String, val startTs: Long)
        val eventKey: (String, String?) -> String = { pkg, cls -> pkg + (cls ?: "") }
        val activeSessions = mutableMapOf<String, InProgress>()

        val queryStart = startMs - BASELINE_EVENT_LOOKBACK_MS
        val events = usageStatsManager.queryEvents(queryStart, endMs) ?: return emptyMap()
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            if (!events.getNextEvent(event)) break
            val pkg = event.packageName ?: continue
            if (pkg in SYSTEM_PACKAGE_BLOCKLIST) continue
            val key = eventKey(pkg, event.className)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    activeSessions[key] = InProgress(pkg, event.timeStamp)
                }
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    val inProgress = activeSessions.remove(key) ?: continue
                    val resumeTs = maxOf(inProgress.startTs, startMs)
                    val pauseTs = minOf(event.timeStamp, endMs)
                    if (pauseTs > resumeTs) {
                        val deltaMs = pauseTs - resumeTs
                        usageMapMs[inProgress.packageName] =
                            usageMapMs.getOrDefault(inProgress.packageName, 0L) + deltaMs
                    }
                }
            }
        }

        activeSessions.values
            .groupBy { it.packageName }
            .forEach { (pkg, sessions) ->
                val latest = sessions.maxByOrNull { it.startTs } ?: return@forEach
                val resumeTs = maxOf(latest.startTs, startMs)
                if (endMs > resumeTs) {
                    usageMapMs[pkg] = usageMapMs.getOrDefault(pkg, 0L) + (endMs - resumeTs)
                }
            }

        return usageMapMs
            .mapValues { (_, ms) -> (ms / 1000).coerceAtLeast(0L) }
            .filterValues { it > 0L }
    }

    /**
     * Import baseline for the last 7 days using event-based reconstruction (Mindful-style).
     * Clears the 7-day window first, then for each day uses getScreenUsageForInterval to compute
     * total foreground seconds per package and stores one SessionEntity per (package, day).
     * Live tracking remains event-based and unchanged.
     */
    suspend fun importBaselineFromUsageStats(context: Context) {
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return
        val now = System.currentTimeMillis()
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)

        val sevenDaysMs = 7L * 24L * 60L * 60L * 1000L
        val baselineWindowStart = now - sevenDaysMs
        val baselineWindowEnd = now + 1L
        sessionDao.deleteSessionsInRange(baselineWindowStart, baselineWindowEnd)

        val deviceId = requireDeviceId()
        var totalRows = 0

        for (dayOffset in 0 until 7) {
            val date = today.minusDays(dayOffset.toLong())
            val dayStartMs = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val dayEndMs = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMs = minOf(dayEndMs, now)
            val usageMap = getScreenUsageForInterval(manager, dayStartMs, endMs)
            for ((pkg, totalSeconds) in usageMap) {
                if (totalSeconds <= 0L) continue
                sessionDao.insertSession(
                    SessionEntity(
                        sessionId = UUID.randomUUID().toString(),
                        deviceId = deviceId,
                        packageName = pkg,
                        startTimestamp = dayStartMs,
                        endTimestamp = endMs,
                        durationSeconds = totalSeconds,
                        createdAt = now
                    )
                )
                totalRows++
            }
        }

        Log.i(TAG, "Imported $totalRows baseline session rows (event-based, 7 days)")
    }

    private suspend fun requireDeviceId(): String {
        val device = deviceDao.getDevice()
            ?: error("Device must be registered before starting sessions")
        return device.deviceId
    }

    companion object {
        private const val TAG = "SessionRepository"
    }
}
