package study.doomscrolling.app.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.entities.OnboardingResponseEntity

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    // Identity Tokens
    var trait1 by mutableStateOf("")
    var trait2 by mutableStateOf("")
    var trait3 by mutableStateOf("")
    var goal1 by mutableStateOf("")
    var goal2 by mutableStateOf("")
    var goal3 by mutableStateOf("")
    var role1 by mutableStateOf("")
    var role2 by mutableStateOf("")
    var role3 by mutableStateOf("")

    // Research Scales (1-5)
    var automaticity by mutableStateOf(3)
    var utility by mutableStateOf(3)
    var intention by mutableStateOf(3)

    var isSubmitting by mutableStateOf(false)
        private set

    fun onTrait1Change(value: String) { trait1 = value }
    fun onTrait2Change(value: String) { trait2 = value }
    fun onTrait3Change(value: String) { trait3 = value }
    fun onGoal1Change(value: String) { goal1 = value }
    fun onGoal2Change(value: String) { goal2 = value }
    fun onGoal3Change(value: String) { goal3 = value }
    fun onRole1Change(value: String) { role1 = value }
    fun onRole2Change(value: String) { role2 = value }
    fun onRole3Change(value: String) { role3 = value }
    fun onAutomaticityChange(value: Int) { automaticity = value }
    fun onUtilityChange(value: Int) { utility = value }
    fun onIntentionChange(value: Int) { intention = value }

    fun submitOnboarding(onComplete: () -> Unit) {
        if (isSubmitting) return
        isSubmitting = true
        
        viewModelScope.launch {
            val device = withContext(Dispatchers.IO) { db.deviceDao().getDevice() }
            val deviceId = device?.deviceId ?: return@launch
            
            val response = OnboardingResponseEntity(
                deviceId = deviceId,
                completedAt = System.currentTimeMillis(),
                trait1 = trait1,
                trait2 = trait2,
                trait3 = trait3,
                goal1 = goal1,
                goal2 = goal2,
                goal3 = goal3,
                role1 = role1,
                role2 = role2,
                role3 = role3,
                automaticity = automaticity,
                utility = utility,
                intention = intention
            )
            
            withContext(Dispatchers.IO) {
                db.onboardingResponseDao().insertOnboardingResponse(response)
            }
            
            isSubmitting = false
            onComplete()
        }
    }
}
