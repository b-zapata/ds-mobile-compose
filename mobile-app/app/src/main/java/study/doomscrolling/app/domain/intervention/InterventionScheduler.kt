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

    val CHECKPOINTS: List<Double> = listOf(0.0, 10.0, 15.0, 20.0)

    private const val MS_PER_MINUTE = 60 * 1000L
    private const val SOFT_WARNING_DELAY_MS = 60 * 1000L // 60 seconds before milestone
    private const val HEADS_UP_DELAY_MS = 10 * 1000L // 10 seconds before milestone

    /**
     * Schedules intervention checks for each checkpoint.
     */
    fun schedule(
        scope: CoroutineScope,
        sessionId: String,
        sessionStartMs: Long,
        engine: InterventionEngine
    ): Job = scope.launch {
        for (checkpointMinutes in CHECKPOINTS) {
            val triggerAtMs = sessionStartMs + (checkpointMinutes * MS_PER_MINUTE).toLong()
            
            val milestoneLabel = if (checkpointMinutes % 1.0 == 0.0) {
                checkpointMinutes.toInt()
            } else {
                checkpointMinutes
            }

            // 1. Handle 60s Soft Notification
            if (checkpointMinutes > 0) {
                val softWarningAtMs = triggerAtMs - SOFT_WARNING_DELAY_MS
                val softWarningDelay = softWarningAtMs - System.currentTimeMillis()
                if (softWarningDelay > 0) {
                    delay(softWarningDelay)
                    Log.i("InterventionScheduler", "Firing 60s soft notification for $milestoneLabel min milestone")
                    engine.showSoftWarningNotification(checkpointMinutes.toInt())
                }
            }

            // 2. Handle 10s Heads-Up Notification
            if (checkpointMinutes > 0) {
                val headsUpAtMs = triggerAtMs - HEADS_UP_DELAY_MS
                val headsUpDelay = headsUpAtMs - System.currentTimeMillis()
                if (headsUpDelay > 0) {
                    delay(headsUpDelay)
                    Log.i("InterventionScheduler", "Firing 10s heads-up notification for $milestoneLabel min milestone")
                    engine.showHeadsUpNotification(checkpointMinutes.toInt())
                }
            }

            // 3. Handle Actual Intervention
            val interventionDelay = triggerAtMs - System.currentTimeMillis()
            if (interventionDelay > 0) {
                delay(interventionDelay)
            }
            engine.triggerIntervention(sessionId, "SESSION_CHECKPOINT", checkpointMinutes.toInt())
        }
    }
}
