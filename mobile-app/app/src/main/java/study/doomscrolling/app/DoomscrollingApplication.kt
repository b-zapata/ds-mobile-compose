package study.doomscrolling.app

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.domain.study.StudyWindow
import study.doomscrolling.app.workers.UploadWorker

class DoomscrollingApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            // Device registration is now handled in ConsentViewModel upon user acceptance.
            syncResearchDataUploadWorkerState()
        }
    }

    private suspend fun syncResearchDataUploadWorkerState() {
        val db = AppDatabase.getInstance(applicationContext)
        val enrolledAt = db.deviceDao().getDevice()?.enrolledAt
        val endAt = StudyWindow.studyEndAt(enrolledAt)
        val workManager = WorkManager.getInstance(this)
        Log.i(TAG, "Upload gate: enrolledAt=$enrolledAt endAt=$endAt now=${System.currentTimeMillis()} studyCompleted=${StudyWindow.isStudyCompleted(enrolledAt)}")

        if (StudyWindow.isStudyCompleted(enrolledAt)) {
            Log.i(TAG, "Study completed; cancelling periodic upload worker")
            workManager.cancelUniqueWork(UPLOAD_WORK_NAME)
            return
        }

        scheduleResearchDataUploadWorker()
    }

    private fun scheduleResearchDataUploadWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val now = ZonedDateTime.now()
        var nextRun = now.withHour(3).withMinute(0).withSecond(0).withNano(0)
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        val delayMinutes = Duration.between(now, nextRun).toMinutes().coerceAtLeast(0)

        val workRequest = PeriodicWorkRequestBuilder<UploadWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            UPLOAD_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE, // Changed to REPLACE to apply the new time immediately
            workRequest
        )
    }

    companion object {
        private const val TAG = "DoomscrollingApp"
        private const val UPLOAD_WORK_NAME = "research-data-upload"
    }
}
