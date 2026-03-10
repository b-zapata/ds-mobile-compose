package study.doomscrolling.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.entities.DeviceEntity
import study.doomscrolling.app.workers.DailyUploadWorker

class DoomscrollingApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            ensureDeviceRegistered()
            scheduleDailyUploadWorker()
        }
    }

    private suspend fun ensureDeviceRegistered() {
        val db = AppDatabase.getInstance(this)
        val device = db.deviceDao().getDevice()
        if (device == null) {
            db.deviceDao().insertDevice(
                // Initial device registration without study arm; arm will be assigned during onboarding.
                DeviceEntity(
                    deviceId = UUID.randomUUID().toString(),
                    studyArm = null,
                    appVersion = BuildConfig.VERSION_NAME ?: "1.0",
                    enrolledAt = null
                )
            )
        }
    }

    private fun scheduleDailyUploadWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val now = ZonedDateTime.now()
        var nextRun = now.withHour(3).withMinute(0).withSecond(0).withNano(0)
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        val delayMinutes = Duration.between(now, nextRun).toMinutes().coerceAtLeast(0)

        val workRequest = PeriodicWorkRequestBuilder<DailyUploadWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_upload_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
