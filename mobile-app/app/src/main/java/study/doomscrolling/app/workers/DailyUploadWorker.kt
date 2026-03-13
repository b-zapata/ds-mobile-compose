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
        // Deprecated by Phase 10 UploadWorker; kept to avoid breaking references if any.
        // The scheduled job is now UploadWorker ("research-data-upload").
        return Result.success()
    }

    companion object {
        // Intentionally unused.
    }
}

