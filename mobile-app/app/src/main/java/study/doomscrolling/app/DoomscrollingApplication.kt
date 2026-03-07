package study.doomscrolling.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.entities.DeviceEntity
import java.util.UUID

class DoomscrollingApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            ensureDeviceRegistered()
        }
    }

    private suspend fun ensureDeviceRegistered() {
        val db = AppDatabase.getInstance(this)
        val device = db.deviceDao().getDevice()
        if (device == null) {
            db.deviceDao().insertDevice(
                DeviceEntity(
                    deviceId = UUID.randomUUID().toString(),
                    createdAt = System.currentTimeMillis(),
                    appVersion = BuildConfig.VERSION_NAME ?: "1.0"
                )
            )
        }
    }
}
