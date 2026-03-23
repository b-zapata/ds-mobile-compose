package study.doomscrolling.app.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import study.doomscrolling.app.BuildConfig
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.domain.study.StudyWindow
import study.doomscrolling.app.data.upload.UploadIntervention
import study.doomscrolling.app.data.upload.UploadPayload
import study.doomscrolling.app.data.upload.UploadPayloadJson
import study.doomscrolling.app.data.upload.UploadService
import study.doomscrolling.app.data.upload.UploadSession

/**
 * Nightly WorkManager worker that uploads sessions + interventions.
 */
class UploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "UPLOAD_WORKER_STARTED")

        val db = AppDatabase.getInstance(applicationContext)
        val device = db.deviceDao().getDevice()
        if (device == null) {
            Log.w(TAG, "UPLOAD_FAILED: no device registered")
            return Result.retry()
        }
        val studyCompleted = StudyWindow.isStudyCompleted(device.enrolledAt)
        if (studyCompleted) {
            Log.i(TAG, "Study completed; running final upload flush")
        }

        val sessions = db.sessionDao().getSessions()
        val interventions = db.interventionDao().getInterventions()

        val payload = UploadPayload(
            deviceId = device.deviceId,
            enrolledAt = device.enrolledAt,
            sessions = sessions.map { s ->
                UploadSession(
                    sessionId = s.sessionId,
                    deviceId = s.deviceId,
                    appPackageName = s.packageName,
                    sessionStartTs = s.startTimestamp,
                    sessionEndTs = s.endTimestamp,
                    durationSeconds = s.durationSeconds
                )
            },
            interventions = interventions.map { i ->
                UploadIntervention(
                    interventionId = i.interventionId,
                    sessionId = i.sessionId,
                    deviceId = i.deviceId,
                    interventionArm = i.interventionArm, // FIX: Pass the arm from local DB
                    milestoneMinutes = i.milestoneMinutes,
                    promptVariant = i.promptVariant,
                    userAction = i.userAction,
                    interventionStartTs = i.interventionStartTs,
                    interventionEndTs = i.interventionEndTs
                )
            }
        )

        val json = UploadPayloadJson.toJsonString(payload)
        Log.i(TAG, "UPLOAD_PAYLOAD_SIZE bytes=${json.toByteArray().size}")

        return try {
            val (code, body) = UploadService().postJson(ENDPOINT, json)
            Log.i(TAG, "UPLOAD_RESPONSE status=$code body=${body.take(2000)}")
            if (code in 200..299) {
                if (studyCompleted) {
                    Log.i(TAG, "Final upload success; cancelling periodic uploads")
                    WorkManager.getInstance(applicationContext).cancelUniqueWork(PERIODIC_UPLOAD_WORK_NAME)
                }
                Log.i(TAG, "UPLOAD_SUCCESS")
                Result.success()
            } else {
                Log.w(TAG, "UPLOAD_FAILED status=$code")
                Result.retry()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "UPLOAD_FAILED exception=${t.message}", t)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "UploadWorker"
        const val PERIODIC_UPLOAD_WORK_NAME = "research-data-upload"
        private const val FINAL_UPLOAD_WORK_NAME = "study-final-upload"
        private val ENDPOINT: String = BuildConfig.INGESTION_URL

        fun enqueueFinalUpload(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<UploadWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                FINAL_UPLOAD_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
