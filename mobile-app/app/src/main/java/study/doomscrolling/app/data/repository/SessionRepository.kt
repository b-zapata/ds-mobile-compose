package study.doomscrolling.app.data.repository

import android.util.Log
import study.doomscrolling.app.BuildConfig
import study.doomscrolling.app.data.dao.DeviceDao
import study.doomscrolling.app.data.dao.SessionDao
import study.doomscrolling.app.data.entities.DeviceEntity
import study.doomscrolling.app.data.entities.SessionEntity
import java.util.UUID

/**
 * Option B: INSERT on session start, UPDATE on session end.
 */
class SessionRepository(
    private val sessionDao: SessionDao,
    private val deviceDao: DeviceDao
) {

    /**
     * Generate sessionId, insert row, return sessionId.
     */
    suspend fun startSession(packageName: String): String {
        val deviceId = requireDeviceId()
        val sessionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = SessionEntity(
            sessionId = sessionId,
            deviceId = deviceId,
            packageName = packageName,
            startTimestamp = now,
            endTimestamp = null,
            durationSeconds = null
        )
        sessionDao.insertSession(entity)
        Log.i(TAG, "DB session inserted")
        return sessionId
    }

    /**
     * Returns the session if it exists and is still active (endTimestamp == null).
     */
    suspend fun getActiveSession(sessionId: String): SessionEntity? =
        sessionDao.getActiveSession(sessionId)?.takeIf { it.endTimestamp == null }

    /**
     * Update endTimestamp and durationSeconds for the given session.
     */
    suspend fun endSession(sessionId: String) {
        val session = sessionDao.getActiveSession(sessionId) ?: return
        val now = System.currentTimeMillis()
        val durationSeconds = ((now - session.startTimestamp) / 1000).coerceAtLeast(0L)
        val updated = session.copy(
            endTimestamp = now,
            durationSeconds = durationSeconds
        )
        sessionDao.updateSession(updated)
        Log.i(TAG, "DB session updated")
    }

    private suspend fun requireDeviceId(): String {
        var device = deviceDao.getDevice()
        if (device == null) {
            device = DeviceEntity(
                deviceId = UUID.randomUUID().toString(),
                createdAt = System.currentTimeMillis(),
                appVersion = BuildConfig.VERSION_NAME ?: "1.0"
            )
            deviceDao.insertDevice(device)
        }
        return device.deviceId
    }

    companion object {
        private const val TAG = "SessionRepository"
    }
}
