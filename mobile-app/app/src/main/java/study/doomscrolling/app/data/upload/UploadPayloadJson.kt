package study.doomscrolling.app.data.upload

import org.json.JSONArray
import org.json.JSONObject
import study.doomscrolling.app.data.entities.OnboardingResponseEntity

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

    /**
     * Wraps an onboarding response into a full Study Payload expected by the server.
     * Includes mandatory empty arrays for sessions and interventions to satisfy server validation.
     */
    fun onboardingToPayloadJsonString(response: OnboardingResponseEntity): String {
        val onboardingObj = JSONObject().apply {
            put("onboarding_version", response.onboardingVersion)
            put("completed_at", response.completedAt)
            
            // Text fields
            put("trait_1", response.trait1)
            put("trait_2", response.trait2)
            put("trait_3", response.trait3)
            put("goal_1", response.goal1)
            put("goal_2", response.goal2)
            put("goal_3", response.goal3)
            put("role_1", response.role1)
            put("role_2", response.role2)
            put("role_3", response.role3)
            
            // Numeric Likert scales
            put("automaticity", response.automaticity)
            put("utility", response.utility)
            put("intention", response.intention)
            put("readiness_reduce_use", response.readinessReduceUse)
            put("willingness_pause_task", response.willingnessPauseTask)
        }

        return JSONObject().apply {
            put("device_id", response.deviceId)
            put("sessions", JSONArray()) // Mandatory empty array
            put("interventions", JSONArray()) // Mandatory empty array
            put("onboarding_response", onboardingObj)
        }.toString()
    }
}
