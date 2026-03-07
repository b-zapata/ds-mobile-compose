package study.doomscrolling.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import study.doomscrolling.app.data.entities.InterventionEntity

@Dao
interface InterventionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntervention(intervention: InterventionEntity)
}
