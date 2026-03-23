package study.doomscrolling.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import study.doomscrolling.app.BuildConfig
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.entities.ExitSurveyResponseEntity
import study.doomscrolling.app.data.upload.UploadPayloadJson
import study.doomscrolling.app.data.upload.UploadService

class ExitSurveyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    // Likert Scales (1-5)
    var interruptionAwareness by mutableStateOf(3)
    var decisionInfluence by mutableStateOf(3)
    var helpfulness by mutableStateOf(3)
    var frustration by mutableStateOf(3)
    var pauseReconsider by mutableStateOf(3)
    var easierToIgnore by mutableStateOf(3)
    var outsideUseLikelihood by mutableStateOf(3)

    // Open Responses
    var biggestInfluenceAspect by mutableStateOf("")
    var ownWordsEffect by mutableStateOf("")
    var suggestions by mutableStateOf("")

    var isSubmitting by mutableStateOf(false)
        private set

    fun submitSurvey(onComplete: () -> Unit) {
        if (isSubmitting) return
        isSubmitting = true
        
        viewModelScope.launch {
            val device = withContext(Dispatchers.IO) { db.deviceDao().getDevice() }
            val deviceId = device?.deviceId ?: return@launch
            
            val response = ExitSurveyResponseEntity(
                deviceId = deviceId,
                completedAt = System.currentTimeMillis(),
                interruptionAwareness = interruptionAwareness,
                decisionInfluence = decisionInfluence,
                helpfulness = helpfulness,
                frustration = frustration,
                pauseReconsider = pauseReconsider,
                easierToIgnore = easierToIgnore,
                outsideUseLikelihood = outsideUseLikelihood,
                biggestInfluenceAspect = biggestInfluenceAspect,
                ownWordsEffect = ownWordsEffect,
                suggestions = suggestions
            )
            
            // 1. Save locally
            withContext(Dispatchers.IO) {
                db.exitSurveyResponseDao().insertExitSurveyResponse(response)
            }

            // 2. Upload to server immediately
            val json = UploadPayloadJson.exitSurveyToPayloadJsonString(response)
            try {
                val (code, responseBody) = withContext(Dispatchers.IO) {
                    UploadService().postJson(BuildConfig.INGESTION_URL, json)
                }
                Log.i("ExitSurveyVM", "Upload successful: $code - $responseBody")
            } catch (e: Exception) {
                Log.e("ExitSurveyVM", "Upload failed", e)
                // Proceed anyway as it's saved locally
            }
            
            isSubmitting = false
            onComplete()
        }
    }
}
