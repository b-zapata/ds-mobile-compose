package study.doomscrolling.app.data.upload

data class UploadPayload(
    val deviceId: String,
    val sessions: List<UploadSession>,
    val interventions: List<UploadIntervention>
)

data class UploadSession(
    val sessionId: String,
    val deviceId: String,
    val appPackageName: String,
    val sessionStartTs: Long,
    val sessionEndTs: Long?,
    val durationSeconds: Long?
)

data class UploadIntervention(
    val interventionId: String,
    val sessionId: String,
    val deviceId: String,
    val milestoneMinutes: Int,
    val promptVariant: Int,
    val userAction: String?,
    val interventionStartTs: Long,
    val interventionEndTs: Long?
)

