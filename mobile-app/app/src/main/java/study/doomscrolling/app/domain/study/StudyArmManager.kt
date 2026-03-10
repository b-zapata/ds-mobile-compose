package study.doomscrolling.app.domain.study

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import study.doomscrolling.app.data.dao.DeviceDao
import study.doomscrolling.app.data.entities.DeviceEntity
import java.util.UUID

enum class StudyArm {
    IDENTITY,
    MINDFULNESS,
    FRICTION,
    BLANK
}

/**
 * Handles random study arm assignment and persistence in the devices table.
 */
class StudyArmManager(
    private val deviceDao: DeviceDao
) {

    /**
     * Assign a study arm if none is set yet. Returns the current arm.
     *
     * If [override] is provided, it will be used for assignment instead of a random arm.
     * When a device already has a study_arm, the override is ignored and the existing
     * assignment is preserved.
     */
    suspend fun assignArmIfNeeded(override: StudyArm? = null): StudyArm = withContext(Dispatchers.IO) {
        val device = deviceDao.getDevice()
        val updated = if (device == null) {
            val arm = override ?: randomArm()
            val now = System.currentTimeMillis()
            val entity = DeviceEntity(
                deviceId = UUID.randomUUID().toString(),
                studyArm = arm.name.lowercase(),
                appVersion = "",
                enrolledAt = now
            )
            deviceDao.insertDevice(entity)
            entity
        } else if (device.studyArm.isNullOrBlank()) {
            val arm = override ?: randomArm()
            val updatedDevice = device.copy(
                studyArm = arm.name.lowercase(),
                enrolledAt = device.enrolledAt ?: System.currentTimeMillis()
            )
            deviceDao.insertDevice(updatedDevice)
            updatedDevice
        } else {
            device
        }
        return@withContext parseArm(updated.studyArm)
    }

    /**
     * Returns the currently assigned arm. If none is assigned yet, this will assign one.
     */
    suspend fun getCurrentArm(): StudyArm = assignArmIfNeeded()

    /**
     * Reset the stored study arm/device row for debugging.
     * Next onboarding run will assign a new arm.
     */
    suspend fun resetStudyArm() = withContext(Dispatchers.IO) {
        deviceDao.resetDevice()
    }

    private fun randomArm(): StudyArm {
        return listOf(
            StudyArm.IDENTITY,
            StudyArm.MINDFULNESS,
            StudyArm.FRICTION,
            StudyArm.BLANK
        ).random()
    }

    private fun parseArm(raw: String?): StudyArm {
        return when (raw?.lowercase()) {
            "identity" -> StudyArm.IDENTITY
            "mindfulness" -> StudyArm.MINDFULNESS
            "friction" -> StudyArm.FRICTION
            "blank" -> StudyArm.BLANK
            else -> StudyArm.BLANK
        }
    }
}

