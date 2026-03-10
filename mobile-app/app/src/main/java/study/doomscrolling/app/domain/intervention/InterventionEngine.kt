package study.doomscrolling.app.domain.intervention

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import study.doomscrolling.app.data.dao.InterventionDao
import study.doomscrolling.app.data.entities.InterventionEntity
import study.doomscrolling.app.data.repository.SessionRepository
import study.doomscrolling.app.domain.prompts.Prompt
import study.doomscrolling.app.domain.prompts.PromptEngine
import study.doomscrolling.app.domain.prompts.PromptManager
import study.doomscrolling.app.domain.study.StudyArmManager
import java.util.UUID

/**
 * Schedules intervention checkpoints during an active session, persists
 * intervention events, and shows the prompt overlay when configured.
 */
class InterventionEngine(
    private val sessionRepository: SessionRepository,
    private val interventionDao: InterventionDao,
    private val studyArmManager: StudyArmManager,
    private val promptEngine: PromptEngine,
    private val promptManager: PromptManager
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
        val session = sessionRepository.getActiveSession(sessionId) ?: return
        val studyArm = studyArmManager.getCurrentArm()
        val milestoneMinutes = checkpointMinutes ?: 0

        val promptInstance = promptEngine.selectPrompt(
            studyArm = studyArm,
            milestoneMinutes = milestoneMinutes
        )
        val now = System.currentTimeMillis()
        val interventionId = UUID.randomUUID().toString()
        val entity = InterventionEntity(
            interventionId = interventionId,
            deviceId = session.deviceId,
            sessionId = sessionId,
            interventionArm = studyArm.name.lowercase(),
            milestoneMinutes = checkpointMinutes,
            promptVariant = promptInstance.promptVariant,
            interventionStartTs = now,
            interventionEndTs = null,
            userAction = null,
            createdAt = now
        )
        interventionDao.insertIntervention(entity)
        Log.i(TAG, "Intervention triggered" + (checkpointMinutes?.let { " at checkpoint $it minutes" } ?: ""))

        val prompt = Prompt(
            id = "arm_${studyArm.name.lowercase()}_${milestoneMinutes}_${promptInstance.promptVariant}",
            text = promptInstance.text,
            category = "milestone_$milestoneMinutes"
        )
        Log.i(TAG, "Prompt selected")
        promptManager.showPrompt(
            prompt = prompt,
            interventionId = interventionId,
            sessionId = sessionId
        )
    }

    /**
     * Called when the overlay completes after the 12-second intervention.
     * Updates end timestamp and user_action based on whether the session is still active.
     */
    fun onInterventionCompleted(interventionId: String, sessionId: String) {
        scope.launch {
            val endTimestamp = System.currentTimeMillis()
            val sessionStillActive = sessionRepository.getActiveSession(sessionId) != null
            val userAction = if (sessionStillActive) {
                "continued_session"
            } else {
                "closed_app"
            }
            interventionDao.updateInterventionCompletion(
                interventionId = interventionId,
                endTimestamp = endTimestamp,
                userAction = userAction
            )
            Log.i(TAG, "Intervention completed for $interventionId with action=$userAction")
        }
    }

    companion object {
        private const val TAG = "InterventionEngine"
    }
}
