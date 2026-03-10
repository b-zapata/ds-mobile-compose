package study.doomscrolling.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.network.DefaultUploadApi
import study.doomscrolling.app.data.network.UploadRepository

class DailyUploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val repository = UploadRepository(
            deviceDao = db.deviceDao(),
            sessionDao = db.sessionDao(),
            interventionDao = db.interventionDao(),
            uploadApi = DefaultUploadApi(INGESTION_URL)
        )
        val success = repository.uploadPendingLogs()
        return if (success) Result.success() else Result.retry()
    }

    companion object {
        // HTTPS endpoint for ingestion API; replace with real endpoint in deployment.
        private const val INGESTION_URL = "https://example.com/api/ingest"
    }
}

