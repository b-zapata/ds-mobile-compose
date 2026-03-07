package study.doomscrolling.app.domain.intervention

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import study.doomscrolling.app.data.dao.InterventionDao
import study.doomscrolling.app.data.entities.InterventionEntity
import study.doomscrolling.app.data.repository.SessionRepository
import java.util.UUID

/**
 * Schedules intervention checkpoints during an active session and persists
 * intervention events. Does not show UI (Phase 6).
 */
class InterventionEngine(
    private val sessionRepository: SessionRepository,
    private val interventionDao: InterventionDao
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitoringJob: kotlinx.coroutines.Job? = null

    /**
     * Start intervention scheduling for this session. Call when session starts.
     */
    fun startMonitoring(sessionId: String, packageName: String, sessionStartMs: Long) {
        stopMonitoring()
        monitoringJob = scope.launch {
            Log.i(TAG, "Intervention scheduler started")
            InterventionScheduler.schedule(this, sessionId, sessionStartMs, this@InterventionEngine).join()
        }
    }

    /**
     * Cancel intervention scheduling. Call when session ends.
     */
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        Log.i(TAG, "Intervention scheduler stopped")
    }

    /**
     * Verify session is still active, then persist intervention and log.
     * Called by [InterventionScheduler] at each checkpoint.
     */
    suspend fun triggerIntervention(
        sessionId: String,
        triggerType: String,
        checkpointMinutes: Int? = null
    ) {
        if (sessionRepository.getActiveSession(sessionId) == null) {
            return
        }
        val entity = InterventionEntity(
                interventionId = UUID.randomUUID().toString(),
                sessionId = sessionId,
                promptId = "placeholder",
                triggerType = triggerType,
                shownTimestamp = System.currentTimeMillis()
            )
        interventionDao.insertIntervention(entity)
        Log.i(TAG, "Intervention triggered" + (checkpointMinutes?.let { " at checkpoint $it minutes" } ?: ""))
    }

    companion object {
        private const val TAG = "InterventionEngine"
    }
}
