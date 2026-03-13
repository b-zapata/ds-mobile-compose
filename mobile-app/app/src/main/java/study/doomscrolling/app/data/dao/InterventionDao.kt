package study.doomscrolling.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import study.doomscrolling.app.data.entities.InterventionEntity

@Dao
interface InterventionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntervention(intervention: InterventionEntity)

    @Query("SELECT * FROM interventions")
    suspend fun getInterventions(): List<InterventionEntity>

    @Query("SELECT * FROM interventions WHERE session_id = :sessionId ORDER BY intervention_start_ts DESC")
    suspend fun getInterventionsForSession(sessionId: String): List<InterventionEntity>

    @Query("DELETE FROM interventions WHERE intervention_id IN (:ids)")
    suspend fun deleteInterventions(ids: List<String>)

    @Query(
        "UPDATE interventions " +
            "SET intervention_end_ts = :endTimestamp, user_action = :userAction " +
            "WHERE intervention_id = :interventionId"
    )
    suspend fun updateInterventionCompletion(
        interventionId: String,
        endTimestamp: Long,
        userAction: String
    )
}
