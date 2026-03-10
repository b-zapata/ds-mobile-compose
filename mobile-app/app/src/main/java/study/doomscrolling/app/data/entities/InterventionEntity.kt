package study.doomscrolling.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interventions")
data class InterventionEntity(
    @PrimaryKey
    @ColumnInfo(name = "intervention_id")
    val interventionId: String,
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "intervention_arm")
    val interventionArm: String,
    @ColumnInfo(name = "milestone_minutes")
    val milestoneMinutes: Int?,
    @ColumnInfo(name = "prompt_variant")
    val promptVariant: Int,
    @ColumnInfo(name = "intervention_start_ts")
    val interventionStartTs: Long,
    @ColumnInfo(name = "intervention_end_ts")
    val interventionEndTs: Long? = null,
    @ColumnInfo(name = "user_action")
    val userAction: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
