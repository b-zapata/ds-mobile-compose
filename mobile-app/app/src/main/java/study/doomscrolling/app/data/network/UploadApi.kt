package study.doomscrolling.app.data.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import study.doomscrolling.app.data.entities.InterventionEntity
import study.doomscrolling.app.data.entities.SessionEntity

interface UploadApi {
    suspend fun uploadLogs(
        deviceId: String,
        sessions: List<SessionEntity>,
        interventions: List<InterventionEntity>
    ): Boolean
}

class DefaultUploadApi(
    private val baseUrl: String
) : UploadApi {

    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun uploadLogs(
        deviceId: String,
        sessions: List<SessionEntity>,
        interventions: List<InterventionEntity>
    ): Boolean {
        val payload = buildPayload(deviceId, sessions, interventions)
        val body = payload.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(baseUrl)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }

    private fun buildPayload(
        deviceId: String,
        sessions: List<SessionEntity>,
        interventions: List<InterventionEntity>
    ): JSONObject {
        val sessionsArray = JSONArray().apply {
            sessions.forEach { session ->
                put(
                    JSONObject().apply {
                        put("session_id", session.sessionId)
                        put("device_id", session.deviceId)
                        put("app_package_name", session.packageName)
                        put("session_start_ts", session.startTimestamp)
                        put("session_end_ts", session.endTimestamp)
                        put("duration_seconds", session.durationSeconds)
                        put("created_at", session.createdAt)
                    }
                )
            }
        }

        val interventionsArray = JSONArray().apply {
            interventions.forEach { intervention ->
                put(
                    JSONObject().apply {
                        put("intervention_id", intervention.interventionId)
                        put("device_id", intervention.deviceId)
                        put("session_id", intervention.sessionId)
                        put("intervention_arm", intervention.interventionArm)
                        put("milestone_minutes", intervention.milestoneMinutes)
                        put("prompt_variant", intervention.promptVariant)
                        put("intervention_start_ts", intervention.interventionStartTs)
                        put("intervention_end_ts", intervention.interventionEndTs)
                        put("user_action", intervention.userAction)
                        put("created_at", intervention.createdAt)
                    }
                )
            }
        }

        return JSONObject().apply {
            put("device_id", deviceId)
            put("sessions", sessionsArray)
            put("interventions", interventionsArray)
        }
    }
}

