package study.doomscrolling.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import study.doomscrolling.app.data.entities.ExitSurveyResponseEntity

@Dao
interface ExitSurveyResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExitSurveyResponse(response: ExitSurveyResponseEntity)

    @Query("SELECT * FROM exit_survey_responses WHERE device_id = :deviceId LIMIT 1")
    suspend fun getExitSurveyResponse(deviceId: String): ExitSurveyResponseEntity?

    @Query("SELECT * FROM exit_survey_responses WHERE device_id = :deviceId LIMIT 1")
    fun observeExitSurveyResponse(deviceId: String): Flow<ExitSurveyResponseEntity?>

    @Query("DELETE FROM exit_survey_responses")
    suspend fun resetExitSurveyResponses()
}
