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
        val sessions = sessionDao.getSessions()
        val interventions = interventionDao.getInterventions()

        if (sessions.isEmpty() && interventions.isEmpty()) {
            return true
        }

        val success = uploadApi.uploadLogs(device.deviceId, sessions, interventions)
        if (success) {
            if (sessions.isNotEmpty()) {
                sessionDao.deleteSessions(sessions.map { it.sessionId })
            }
            if (interventions.isNotEmpty()) {
                interventionDao.deleteInterventions(interventions.map { it.interventionId })
            }
        }
        return success
    }
}

