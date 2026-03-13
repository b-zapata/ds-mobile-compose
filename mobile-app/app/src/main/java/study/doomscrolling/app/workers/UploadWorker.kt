package study.doomscrolling.app.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.upload.UploadIntervention
import study.doomscrolling.app.data.upload.UploadPayload
import study.doomscrolling.app.data.upload.UploadPayloadJson
import study.doomscrolling.app.data.upload.UploadService
import study.doomscrolling.app.data.upload.UploadSession

/**
 * Nightly WorkManager worker that uploads sessions + interventions.
 *
 * Phase 10: query Room, serialize to JSON, POST to HTTPS endpoint, log result.
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

        val sessions = db.sessionDao().getSessions()
        val interventions = db.interventionDao().getInterventions()

        val payload = UploadPayload(
            deviceId = device.deviceId,
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
            val (code, body) = UploadService().postJson(TEST_ENDPOINT, json)
            Log.i(TAG, "UPLOAD_RESPONSE status=$code body=${body.take(2000)}")
            if (code in 200..299) {
                Log.i(TAG, "UPLOAD_SUCCESS")
                // Placeholder for Phase 11/12: mark rows as uploaded instead of deleting.
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
        private const val TEST_ENDPOINT = "https://httpbin.org/post"
    }
}
