package study.doomscrolling.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import study.doomscrolling.app.data.entities.DeviceEntity

@Dao
interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)

    @Query("SELECT * FROM devices LIMIT 1")
    suspend fun getDevice(): DeviceEntity?

    @Query("DELETE FROM devices")
    suspend fun resetDevice()
}
