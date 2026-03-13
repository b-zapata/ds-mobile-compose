package study.doomscrolling.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Monitored app session (Option B: INSERT on start, UPDATE on end).
 */
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["device_id"],
            childColumns = ["device_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["device_id"]),
        Index(value = ["app_package_name"]),
        Index(value = ["session_start_ts"])
    ]
)
data class SessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    @ColumnInfo(name = "app_package_name")
    val packageName: String,
    @ColumnInfo(name = "session_start_ts")
    val startTimestamp: Long,
    @ColumnInfo(name = "session_end_ts")
    val endTimestamp: Long? = null,
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
