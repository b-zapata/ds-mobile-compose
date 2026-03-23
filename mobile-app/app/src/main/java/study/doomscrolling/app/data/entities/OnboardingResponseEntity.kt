package study.doomscrolling.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "onboarding_responses")
data class OnboardingResponseEntity(
    @PrimaryKey
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Long,

    @ColumnInfo(name = "onboarding_version")
    val onboardingVersion: String? = "1.0",

    // Identity Tokens (Arm A)
    @ColumnInfo(name = "trait_1") val trait1: String,
    @ColumnInfo(name = "trait_2") val trait2: String,
    @ColumnInfo(name = "trait_3") val trait3: String,

    @ColumnInfo(name = "goal_1") val goal1: String,
    @ColumnInfo(name = "goal_2") val goal2: String,
    @ColumnInfo(name = "goal_3") val goal3: String,

    @ColumnInfo(name = "role_1") val role1: String,
    @ColumnInfo(name = "role_2") val role2: String,
    @ColumnInfo(name = "role_3") val role3: String,

    // Research Scales (General)
    @ColumnInfo(name = "automaticity") val automaticity: Int,
    @ColumnInfo(name = "utility") val utility: Int,
    @ColumnInfo(name = "intention") val intention: Int,
    
    @ColumnInfo(name = "readiness_reduce_use") val readinessReduceUse: Int,
    @ColumnInfo(name = "willingness_pause_task") val willingnessPauseTask: Int
)
