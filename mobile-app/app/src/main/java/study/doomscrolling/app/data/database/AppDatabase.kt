package study.doomscrolling.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import study.doomscrolling.app.data.dao.*
import study.doomscrolling.app.data.entities.*

@Database(
    entities = [
        DeviceEntity::class,
        SessionEntity::class,
        InterventionEntity::class,
        OnboardingResponseEntity::class,
        ExitSurveyResponseEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun sessionDao(): SessionDao
    abstract fun interventionDao(): InterventionDao
    abstract fun onboardingResponseDao(): OnboardingResponseDao
    abstract fun exitSurveyResponseDao(): ExitSurveyResponseDao

    companion object {
        private const val DB_NAME = "doomscrolling-db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
