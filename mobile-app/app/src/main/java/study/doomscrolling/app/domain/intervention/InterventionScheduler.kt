package study.doomscrolling.app.domain.intervention

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Checkpoints (minutes since session start) at which interventions may trigger.
 */
object InterventionScheduler {

    // DEBUG: Changed from [0, 10, 15, 20] to [0, 2, 5, 10] for faster testing
    val CHECKPOINTS: List<Int> = listOf(0, 2, 5, 10)

    private const val MS_PER_MINUTE = 60 * 1000L
    private const val SOFT_WARNING_DELAY_MS = 60 * 1000L // 60 seconds before milestone
    private const val HEADS_UP_DELAY_MS = 10 * 1000L // 10 seconds before milestone

    /**
     * Schedules intervention checks for each checkpoint. For each checkpoint,
     * delays until that many minutes after sessionStart, then calls
     * [InterventionEngine.triggerIntervention] if still running.
     */
    fun schedule(
        scope: CoroutineScope,
        sessionId: String,
        sessionStartMs: Long,
        engine: InterventionEngine
    ): Job = scope.launch {
        for (checkpointMinutes in CHECKPOINTS) {
            val triggerAtMs = sessionStartMs + (checkpointMinutes * MS_PER_MINUTE)
            
            // 1. Handle 60s Soft Notification (Silent)
            if (checkpointMinutes > 0) {
                val softWarningAtMs = triggerAtMs - SOFT_WARNING_DELAY_MS
                val softWarningDelay = softWarningAtMs - System.currentTimeMillis()
                if (softWarningDelay > 0) {
                    delay(softWarningDelay)
                    Log.i("InterventionScheduler", "Firing 60s soft notification for $checkpointMinutes min milestone")
                    engine.showSoftWarningNotification(checkpointMinutes)
                }
            }

            // 2. Handle 10s Heads-Up Notification (Vignette)
            if (checkpointMinutes > 0) {
                val headsUpAtMs = triggerAtMs - HEADS_UP_DELAY_MS
                val headsUpDelay = headsUpAtMs - System.currentTimeMillis()
                if (headsUpDelay > 0) {
                    delay(headsUpDelay)
                    Log.i("InterventionScheduler", "Firing 10s heads-up notification for $checkpointMinutes min milestone")
                    engine.showHeadsUpNotification(checkpointMinutes)
                }
            }

            // 3. Handle Actual Intervention
            val interventionDelay = triggerAtMs - System.currentTimeMillis()
            if (interventionDelay > 0) {
                delay(interventionDelay)
            }
            engine.triggerIntervention(sessionId, "SESSION_CHECKPOINT", checkpointMinutes)
        }
    }
}
