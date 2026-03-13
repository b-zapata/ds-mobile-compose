package study.doomscrolling.app.data.upload

import org.json.JSONArray
import org.json.JSONObject

object UploadPayloadJson {
    fun toJsonString(payload: UploadPayload): String {
        val sessionsArray = JSONArray().apply {
            payload.sessions.forEach { s ->
                put(
                    JSONObject().apply {
                        put("session_id", s.sessionId)
                        put("device_id", s.deviceId)
                        put("app_package_name", s.appPackageName)
                        put("session_start_ts", s.sessionStartTs)
                        put("session_end_ts", s.sessionEndTs)
                        put("duration_seconds", s.durationSeconds)
                    }
                )
            }
        }

        val interventionsArray = JSONArray().apply {
            payload.interventions.forEach { i ->
                put(
                    JSONObject().apply {
                        put("intervention_id", i.interventionId)
                        put("session_id", i.sessionId)
                        put("device_id", i.deviceId)
                        put("milestone_minutes", i.milestoneMinutes)
                        put("prompt_variant", i.promptVariant)
                        put("user_action", i.userAction)
                        put("intervention_start_ts", i.interventionStartTs)
                        put("intervention_end_ts", i.interventionEndTs)
                    }
                )
            }
        }

        return JSONObject().apply {
            put("device_id", payload.deviceId)
            put("sessions", sessionsArray)
            put("interventions", interventionsArray)
        }.toString()
    }
}

