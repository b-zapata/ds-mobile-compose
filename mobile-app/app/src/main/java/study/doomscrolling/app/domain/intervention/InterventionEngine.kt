package study.doomscrolling.app.domain.intervention

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import study.doomscrolling.app.R
import study.doomscrolling.app.data.dao.InterventionDao
import study.doomscrolling.app.data.dao.OnboardingResponseDao
import study.doomscrolling.app.data.entities.InterventionEntity
import study.doomscrolling.app.data.repository.SessionRepository
import study.doomscrolling.app.domain.prompts.Prompt
import study.doomscrolling.app.domain.prompts.PromptEngine
import study.doomscrolling.app.domain.prompts.PromptManager
import study.doomscrolling.app.domain.study.StudyArm
import study.doomscrolling.app.domain.study.StudyArmManager
import java.util.UUID

/**
 * Schedules intervention checkpoints during an active session, persists
 * intervention events, and shows the prompt overlay when configured.
 */
class InterventionEngine(
    private val context: Context,
    private val sessionRepository: SessionRepository,
    private val interventionDao: InterventionDao,
    private val onboardingResponseDao: OnboardingResponseDao,
    private val studyArmManager: StudyArmManager,
    private val promptEngine: PromptEngine,
    private val promptManager: PromptManager
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitoringJob: kotlinx.coroutines.Job? = null
    
    @Volatile
    private var isHeadsUpActive: Boolean = false

    init {
        createNotificationChannels()
    }

    fun isHeadsUpPending(): Boolean = isHeadsUpActive

    /**
     * Start intervention scheduling for this session.
     */
    fun startMonitoring(sessionId: String, packageName: String, sessionStartMs: Long) {
        stopMonitoring()
        monitoringJob = scope.launch {
            Log.i(TAG, "Intervention scheduler started")
            InterventionScheduler.schedule(this, sessionId, sessionStartMs, this@InterventionEngine).join()
        }
    }

    /**
     * Cancel intervention scheduling.
     */
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        isHeadsUpActive = false
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(SOFT_WARNING_NOTIFICATION_ID)
        nm.cancel(HEADS_UP_NOTIFICATION_ID)
        Log.i(TAG, "Intervention scheduler stopped")
    }

    /**
     * T-minus 60s: High-priority vignette, but silent.
     */
    fun showSoftWarningNotification(milestoneMinutes: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, SOFT_WARNING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Reflection point approaching")
            .setContentText("A reflection pause will occur in 1 minute.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) 
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(null, true) // Force peek/vignette
            .setAutoCancel(true)
            .build()

        notificationManager.notify(SOFT_WARNING_NOTIFICATION_ID, notification)
    }

    /**
     * T-minus 10s: High-priority vignette with alert sound.
     */
    fun showHeadsUpNotification(milestoneMinutes: Int) {
        isHeadsUpActive = true
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(SOFT_WARNING_NOTIFICATION_ID)

        val notification = NotificationCompat.Builder(context, HEADS_UP_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Study Pause Coming")
            .setContentText("A reflection pause will start in 10 seconds.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) 
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(null, true) // Force peek/vignette
            .setAutoCancel(true)
            .build()

        notificationManager.notify(HEADS_UP_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // v5 Soft Channel: High Importance (Peeking) but SILENT
            val softChannel = NotificationChannel(
                SOFT_WARNING_CHANNEL_ID,
                "Study Reflection Warnings",
                NotificationManager.IMPORTANCE_HIGH 
            ).apply {
                description = "Silent peeking alerts 1 minute before a study pause"
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            // v5 Heads-Up Channel: High Importance (Peeking) with SOUND
            val headsUpChannel = NotificationChannel(
                HEADS_UP_CHANNEL_ID,
                "Heads-Up Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Slide-down warnings 10 seconds before a study pause"
            }
            
            notificationManager.createNotificationChannel(softChannel)
            notificationManager.createNotificationChannel(headsUpChannel)
        }
    }

    suspend fun triggerIntervention(
        sessionId: String,
        triggerType: String,
        checkpointMinutes: Int? = null
    ) {
        isHeadsUpActive = false
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(SOFT_WARNING_NOTIFICATION_ID)
        nm.cancel(HEADS_UP_NOTIFICATION_ID)

        val session = sessionRepository.getActiveSession(sessionId) ?: return
        val onboarding = onboardingResponseDao.getOnboardingResponse(session.deviceId)
        if (onboarding == null) {
            Log.i(TAG, "Skipping intervention: Onboarding not completed yet.")
            return
        }

        // DEBUG: Force FRICTION arm for all interventions
        val studyArm = StudyArm.FRICTION
        val milestoneMinutes = checkpointMinutes ?: 0

        val personalization = if (studyArm == StudyArm.IDENTITY) {
            mapOf(
                "Trait 1" to onboarding.trait1,
                "Trait 2" to onboarding.trait2,
                "Trait 3" to onboarding.trait3,
                "Goal 1" to onboarding.goal1,
                "Goal 2" to onboarding.goal2,
                "Goal 3" to onboarding.goal3,
                "Role 1" to onboarding.role1,
                "Role 2" to onboarding.role2,
                "Role 3" to onboarding.role3
            )
        } else {
            emptyMap()
        }

        val promptInstance = promptEngine.selectPrompt(
            studyArm = studyArm,
            milestoneMinutes = milestoneMinutes,
            personalization = personalization
        )
        
        val now = System.currentTimeMillis()
        val interventionId = UUID.randomUUID().toString()
        val entity = InterventionEntity(
            interventionId = interventionId,
            deviceId = session.deviceId,
            sessionId = sessionId,
            interventionArm = studyArm.name.lowercase(),
            milestoneMinutes = milestoneMinutes,
            promptVariant = promptInstance.promptVariant,
            interventionStartTs = now,
            interventionEndTs = null,
            userAction = null,
            createdAt = now
        )
        interventionDao.insertIntervention(entity)

        val prompt = Prompt(
            id = "arm_${studyArm.name.lowercase()}_${milestoneMinutes}_${promptInstance.promptVariant}",
            text = promptInstance.text,
            category = "milestone_$milestoneMinutes",
            arm = studyArm
        )
        promptManager.showPrompt(
            prompt = prompt,
            interventionId = interventionId,
            sessionId = sessionId
        )
    }

    fun onInterventionCompleted(interventionId: String, sessionId: String, action: String) {
        scope.launch {
            val endTimestamp = System.currentTimeMillis()
            interventionDao.updateInterventionCompletion(
                interventionId = interventionId,
                endTimestamp = endTimestamp,
                userAction = action
            )
            Log.i(TAG, "Intervention completed for $interventionId with action=$action")
        }
    }

    companion object {
        private const val TAG = "InterventionEngine"
        private const val SOFT_WARNING_CHANNEL_ID = "soft_warning_intervention_v5"
        private const val HEADS_UP_CHANNEL_ID = "heads_up_intervention_v5"
        private const val SOFT_WARNING_NOTIFICATION_ID = 1000
        private const val HEADS_UP_NOTIFICATION_ID = 1001
    }
}
