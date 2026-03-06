package study.doomscrolling.app.services

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import study.doomscrolling.app.domain.MonitoredApps

/**
 * Determines the current foreground app by reading UsageEvents and
 * reconstructing foreground from ACTIVITY_RESUMED / ACTIVITY_PAUSED.
 */
class ForegroundAppDetector(private val context: Context) {

    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    /**
     * Returns the package name of the app currently in the foreground,
     * or null if unknown or no app.
     */
    fun getCurrentForegroundPackage(): String? {
        val manager = usageStatsManager ?: return null
        val now = System.currentTimeMillis()
        val start = now - QUERY_WINDOW_MS

        val usageEvents = manager.queryEvents(start, now) ?: return null
        var lastResumedPackage: String? = null

        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            if (!usageEvents.getNextEvent(event)) break
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> lastResumedPackage = event.packageName
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    // Another app is taking over; last ACTIVITY_RESUMED will be the new one
                }
            }
        }
        return lastResumedPackage
    }

    companion object {
        /** UsageEvents query window (10s) to reduce batching/transition noise. */
        private const val QUERY_WINDOW_MS = 10_000L

        /**
         * Returns true if this app has been granted usage stats access (Settings → Usage access).
         */
        fun hasUsageStatsPermission(context: Context): Boolean {
            val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return false
            val now = System.currentTimeMillis()
            val stats = manager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - 60_000,
                now
            ) ?: return false
            return stats.isNotEmpty()
        }
    }
}
