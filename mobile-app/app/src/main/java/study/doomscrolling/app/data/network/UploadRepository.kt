package study.doomscrolling.app.data.network

import study.doomscrolling.app.data.dao.DeviceDao
import study.doomscrolling.app.data.dao.InterventionDao
import study.doomscrolling.app.data.dao.SessionDao

class UploadRepository(
    private val deviceDao: DeviceDao,
    private val sessionDao: SessionDao,
    private val interventionDao: InterventionDao,
    private val uploadApi: UploadApi
) {

    suspend fun uploadPendingLogs(): Boolean {
        val device = deviceDao.getDevice() ?: return false
        val sessions = sessionDao.getPendingSessions()
        val interventions = interventionDao.getPendingInterventions()

        if (sessions.isEmpty() && interventions.isEmpty()) {
            return true
        }

        val success = uploadApi.uploadLogs(device.deviceId, sessions, interventions)
        if (success) {
            val uploadedAt = System.currentTimeMillis()
            if (sessions.isNotEmpty()) {
                sessionDao.markSessionsUploaded(sessions.map { it.sessionId }, uploadedAt)
            }
            if (interventions.isNotEmpty()) {
                interventionDao.markInterventionsUploaded(interventions.map { it.interventionId }, uploadedAt)
            }
        }
        return success
    }
}

