package study.doomscrolling.app.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import study.doomscrolling.app.BuildConfig
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.domain.study.StudyArm
import study.doomscrolling.app.domain.study.StudyArmManager

/**
 * ViewModel for onboarding flow. In debug builds it allows overriding the
 * study arm assignment for testing.
 */
class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    enum class DebugArmOption(val label: String) {
        RANDOM("Random"),
        IDENTITY("Identity"),
        MINDFULNESS("Mindfulness"),
        FRICTION("Friction"),
        BLANK("Blank")
    }

    var selectedDebugArm: DebugArmOption by mutableStateOf(DebugArmOption.RANDOM)
        private set

    private val studyArmManager: StudyArmManager by lazy {
        val db = AppDatabase.getInstance(getApplication())
        StudyArmManager(db.deviceDao())
    }

    fun onDebugArmSelected(option: DebugArmOption) {
        selectedDebugArm = option
    }

    /**
     * Apply study arm assignment when onboarding completes.
     *
     * In production builds this preserves normal randomization.
     * In debug builds it uses the selected override when not RANDOM.
     */
    fun applyStudyArmOverrideIfNeeded() {
        viewModelScope.launch {
            if (BuildConfig.DEBUG) {
                val override = when (selectedDebugArm) {
                    DebugArmOption.RANDOM -> null
                    DebugArmOption.IDENTITY -> StudyArm.IDENTITY
                    DebugArmOption.MINDFULNESS -> StudyArm.MINDFULNESS
                    DebugArmOption.FRICTION -> StudyArm.FRICTION
                    DebugArmOption.BLANK -> StudyArm.BLANK
                }
                studyArmManager.assignArmIfNeeded(override)
            } else {
                // Production: normal random assignment behavior.
                studyArmManager.assignArmIfNeeded()
            }
        }
    }

    /**
     * Debug-only reset of the stored study arm/device row.
     * Does not touch sessions or interventions.
     */
    fun resetStudyArm() {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch {
            studyArmManager.resetStudyArm()
            // Reset UI selection back to Random so next onboarding behaves like fresh assignment.
            selectedDebugArm = DebugArmOption.RANDOM
        }
    }
}

