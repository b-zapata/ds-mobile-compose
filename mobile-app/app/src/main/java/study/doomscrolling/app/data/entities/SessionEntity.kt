package study.doomscrolling.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Monitored app session (Option B: INSERT on start, UPDATE on end).
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    val deviceId: String,
    val packageName: String,
    val startTimestamp: Long,
    val endTimestamp: Long? = null,
    val durationSeconds: Long? = null
)
