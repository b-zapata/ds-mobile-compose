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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<SessionEntity>)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE session_id = :sessionId LIMIT 1")
    suspend fun getActiveSession(sessionId: String): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY session_start_ts DESC")
    suspend fun getSessions(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE uploaded_at IS NULL ORDER BY session_start_ts DESC")
    suspend fun getPendingSessions(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE device_id = :deviceId ORDER BY session_start_ts DESC")
    suspend fun getSessionsForDevice(deviceId: String): List<SessionEntity>

    /**
     * Most recent ended session for this device+package that ended no earlier than minEndTimestamp.
     * Used to extend (merge) sessions when the same app returns within the merge window.
     */
    @Query(
        "SELECT * FROM sessions " +
            "WHERE device_id = :deviceId AND app_package_name = :packageName " +
            "AND session_end_ts >= :minEndTimestamp " +
            "ORDER BY session_end_ts DESC LIMIT 1"
    )
    suspend fun getMostRecentEndedSession(
        deviceId: String,
        packageName: String,
        minEndTimestamp: Long
    ): SessionEntity?

    @Query("DELETE FROM sessions WHERE session_id IN (:ids)")
    suspend fun deleteSessions(ids: List<String>)

    @Query("UPDATE sessions SET uploaded_at = :uploadedAt WHERE session_id IN (:ids)")
    suspend fun markSessionsUploaded(ids: List<String>, uploadedAt: Long)

    /** Delete sessions that started in [startMs, endMs). Used to clear baseline window before re-import. */
    @Query("DELETE FROM sessions WHERE session_start_ts >= :startMs AND session_start_ts < :endMs")
    suspend fun deleteSessionsInRange(startMs: Long, endMs: Long)
}
