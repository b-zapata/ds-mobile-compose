package study.doomscrolling.app.domain.study

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import study.doomscrolling.app.data.dao.DeviceDao

enum class StudyArm {
    IDENTITY,
    MINDFULNESS,
    FRICTION,
    CONTROL
}

/**
 * Handles random study arm assignment.
 * Since the study is now within-subject, this manager provides a random arm
 * for each intervention session rather than a fixed one for the device.
 */
class StudyArmManager(
    private val deviceDao: DeviceDao
) {

    /**
     * Returns a random study arm for the current intervention.
     */
    fun getRandomArm(): StudyArm {
        return listOf(
            StudyArm.IDENTITY,
            StudyArm.MINDFULNESS,
            StudyArm.FRICTION,
            StudyArm.CONTROL
        ).random()
    }

    /**
     * Legacy method for getting the "current" arm. 
     * Now returns a random arm to support within-subject design.
     */
    fun getCurrentArm(): StudyArm = getRandomArm()

    /**
     * Reset the stored device row for debugging.
     */
    suspend fun resetStudyArm() = withContext(Dispatchers.IO) {
        deviceDao.resetDevice()
    }
}
