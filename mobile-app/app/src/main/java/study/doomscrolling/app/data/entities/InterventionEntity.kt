package study.doomscrolling.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Intervention record (used in Phase 5/6).
 */
@Entity(tableName = "interventions")
data class InterventionEntity(
    @PrimaryKey val interventionId: String,
    val sessionId: String,
    val promptId: String,
    val triggerType: String,
    val shownTimestamp: Long
)
