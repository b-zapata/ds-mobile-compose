package study.doomscrolling.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Device participating in the study.
 */
@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    @ColumnInfo(name = "study_arm")
    val studyArm: String?,
    @ColumnInfo(name = "app_version")
    val appVersion: String,
    @ColumnInfo(name = "enrolled_at")
    val enrolledAt: Long?
)
