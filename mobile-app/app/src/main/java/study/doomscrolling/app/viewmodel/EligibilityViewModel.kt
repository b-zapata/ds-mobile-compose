package study.doomscrolling.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import study.doomscrolling.app.BuildConfig
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.upload.UploadIntervention
import study.doomscrolling.app.data.upload.UploadPayload
import study.doomscrolling.app.data.upload.UploadPayloadJson
import study.doomscrolling.app.data.upload.UploadService
import study.doomscrolling.app.data.upload.UploadSession
import study.doomscrolling.app.data.repository.SessionRepository
import study.doomscrolling.app.domain.MonitoredApps
import study.doomscrolling.app.services.UsageTrackingService
import java.time.Instant
import java.time.ZoneId

data class EligibilityUiState(
    val checking: Boolean = false,
    val eligible: Boolean? = null,
    val message: String? = null
)

class EligibilityViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase by lazy { AppDatabase.getInstance(getApplication()) }
    private val sessionRepository: SessionRepository by lazy {
        SessionRepository(db.sessionDao(), db.deviceDao())
    }

    private val _uiState = MutableStateFlow(EligibilityUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Run baseline import for the last 7 days, evaluate eligibility (used any monitored app
     * on at least 5 of the last 7 days), and if eligible upload the baseline sessions to the server.
     */
    fun checkEligibilityAndUploadBaseline() {
        if (_uiState.value.checking) return
        viewModelScope.launch {
            _uiState.value = EligibilityUiState(checking = true, eligible = null, message = "Checking eligibility and usage history…")

            // 1. Populate baseline sessions table for last 7 days.
            withContext(Dispatchers.IO) {
                sessionRepository.importBaselineFromUsageStats(getApplication())
            }

            val now = System.currentTimeMillis()
            val sevenDaysMs = 7L * 24L * 60L * 60L * 1000L
            val since = now - sevenDaysMs

            val allSessions = withContext(Dispatchers.IO) {
                db.sessionDao().getSessions()
            }
            
            // 2. Filter sessions in the last 7 days
            val allRecent = allSessions.filter { it.startTimestamp >= since }
            
            // 3. Just monitored sessions (for eligibility check)
            val monitoredRecent = allRecent.filter { MonitoredApps.isMonitored(it.packageName) }

            if (monitoredRecent.isEmpty()) {
                _uiState.value = EligibilityUiState(
                    checking = false,
                    eligible = false,
                    message = "You are not eligible because you have not used any of the target apps in the last 7 days."
                )
                return@launch
            }

            val zoneId = ZoneId.systemDefault()
            val activeDays = monitoredRecent
                .map { Instant.ofEpochMilli(it.startTimestamp).atZone(zoneId).toLocalDate() }
                .toSet()
                .size

            val eligible = activeDays >= 5
            if (!eligible) {
                _uiState.value = EligibilityUiState(
                    checking = false,
                    eligible = false,
                    message = "You are not eligible because you have not used any of the target apps on at least 5 of the last 7 days. (Found: $activeDays days)"
                )
                return@launch
            }

            // 4. If eligible, upload baseline sessions to the ingestion API.
            val device = withContext(Dispatchers.IO) {
                db.deviceDao().getDevice()
            }
            val deviceId = device?.deviceId
            if (deviceId == null) {
                _uiState.value = EligibilityUiState(
                    checking = false,
                    eligible = true,
                    message = "Eligibility passed, but device ID missing. Please sign the consent form first."
                )
                return@launch
            }

            val baselineSessions = allRecent.map { s ->
                UploadSession(
                    sessionId = s.sessionId,
                    deviceId = s.deviceId,
                    appPackageName = s.packageName,
                    sessionStartTs = s.startTimestamp,
                    sessionEndTs = s.endTimestamp,
                    durationSeconds = s.durationSeconds
                )
            }

            val payload = UploadPayload(
                deviceId = deviceId,
                enrolledAt = device.enrolledAt ?: System.currentTimeMillis(),
                sessions = baselineSessions,
                interventions = emptyList<UploadIntervention>()
            )

            val json = UploadPayloadJson.toJsonString(payload)
            val (code, responseBody) = UploadService().postJson(BuildConfig.INGESTION_URL, json)

            if (code in 200..299) {
                // MARK ELIGIBILITY AS PASSED by setting enrolledAt
                withContext(Dispatchers.IO) {
                    val updatedDevice = device.copy(enrolledAt = System.currentTimeMillis())
                    db.deviceDao().insertDevice(updatedDevice)
                }
                UsageTrackingService.startOrRefresh(getApplication())

                _uiState.value = EligibilityUiState(
                    checking = false,
                    eligible = true,
                    message = "Success! You are eligible and your baseline data has been uploaded. Redirecting..."
                )
            } else {
                _uiState.value = EligibilityUiState(
                    checking = false,
                    eligible = null,
                    message = "Eligibility passed, but we couldn't connect to the server (Error $code). Please check your internet and try again."
                )
            }
        }
    }
}
