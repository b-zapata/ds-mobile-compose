package study.doomscrolling.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exit_survey_responses")
data class ExitSurveyResponseEntity(
    @PrimaryKey
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Long,

    // Likert Scales (1-5)
    @ColumnInfo(name = "interruption_awareness") val interruptionAwareness: Int,
    @ColumnInfo(name = "decision_influence") val decisionInfluence: Int,
    @ColumnInfo(name = "helpfulness") val helpfulness: Int,
    @ColumnInfo(name = "frustration") val frustration: Int,
    @ColumnInfo(name = "pause_reconsider") val pauseReconsider: Int,
    @ColumnInfo(name = "easier_to_ignore") val easierToIgnore: Int,
    @ColumnInfo(name = "outside_use_likelihood") val outsideUseLikelihood: Int,

    // Open Responses
    @ColumnInfo(name = "biggest_influence_aspect") val biggestInfluenceAspect: String,
    @ColumnInfo(name = "own_words_effect") val ownWordsEffect: String,
    @ColumnInfo(name = "suggestions") val suggestions: String
)
