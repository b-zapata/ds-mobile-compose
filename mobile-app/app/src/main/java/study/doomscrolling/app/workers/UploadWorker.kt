package study.doomscrolling.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Placeholder WorkManager worker for nightly data upload.
 * Will serialize sessions and interventions and send to ingestion API (Phase 10).
 */
class UploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = Result.success()
}
