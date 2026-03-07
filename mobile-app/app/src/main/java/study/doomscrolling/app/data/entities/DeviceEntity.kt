package study.doomscrolling.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Device participating in the study.
 */
@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val deviceId: String,
    val createdAt: Long,
    val appVersion: String
)
