package study.doomscrolling.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import study.doomscrolling.app.data.entities.OnboardingResponseEntity

@Dao
interface OnboardingResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnboardingResponse(response: OnboardingResponseEntity)

    @Query("SELECT * FROM onboarding_responses WHERE device_id = :deviceId LIMIT 1")
    suspend fun getOnboardingResponse(deviceId: String): OnboardingResponseEntity?

    @Query("DELETE FROM onboarding_responses")
    suspend fun resetOnboardingResponses()
}
