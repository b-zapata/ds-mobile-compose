package study.doomscrolling.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import study.doomscrolling.app.data.entities.SessionEntity

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getActiveSession(sessionId: String): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY startTimestamp DESC")
    suspend fun getSessions(): List<SessionEntity>
}
