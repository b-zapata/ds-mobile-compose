package study.doomscrolling.app.domain.intervention

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Checkpoints (minutes since session start) at which interventions may trigger.
 */
object InterventionScheduler {

    val CHECKPOINTS: List<Int> = listOf(0, 10, 15, 20)

    private const val MS_PER_MINUTE = 60 * 1000L

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
            val delayMs = triggerAtMs - System.currentTimeMillis()
            if (delayMs > 0) {
                delay(delayMs)
            }
            engine.triggerIntervention(sessionId, "SESSION_CHECKPOINT", checkpointMinutes)
        }
    }
}
