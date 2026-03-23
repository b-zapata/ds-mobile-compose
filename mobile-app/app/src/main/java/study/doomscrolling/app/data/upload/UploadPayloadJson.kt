package study.doomscrolling.app.data.upload

import org.json.JSONArray
import org.json.JSONObject
import study.doomscrolling.app.data.entities.ExitSurveyResponseEntity
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
                        put("session_end_ts", s.sessionEndTs ?: JSONObject.NULL)
                        put("duration_seconds", s.durationSeconds ?: JSONObject.NULL)
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
                        put("intervention_arm", i.interventionArm) // FIX: Added arm to JSON
                        put("milestone_minutes", i.milestoneMinutes)
                        put("prompt_variant", i.promptVariant)
                        put("user_action", i.userAction ?: JSONObject.NULL)
                        put("intervention_start_ts", i.interventionStartTs)
                        put("intervention_end_ts", i.interventionEndTs ?: JSONObject.NULL)
                    }
                )
            }
        }

        return JSONObject().apply {
            put("device_id", payload.deviceId)
            put("enrolled_at", payload.enrolledAt ?: JSONObject.NULL)
            put("sessions", sessionsArray)
            put("interventions", interventionsArray)
        }.toString()
    }

    /**
     * Wraps an onboarding response into a full Study Payload expected by the server.
     */
    fun onboardingToPayloadJsonString(response: OnboardingResponseEntity): String {
        val onboardingObj = JSONObject().apply {
            put("onboarding_version", response.onboardingVersion ?: JSONObject.NULL)
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
            put("sessions", JSONArray()) 
            put("interventions", JSONArray()) 
            put("onboarding_response", onboardingObj)
        }.toString()
    }

    /**
     * Wraps an exit survey response into a full Study Payload expected by the server.
     */
    fun exitSurveyToPayloadJsonString(response: ExitSurveyResponseEntity): String {
        val exitObj = JSONObject().apply {
            put("completed_at", response.completedAt)
            
            // Likert Scales
            put("interruption_awareness", response.interruptionAwareness)
            put("decision_influence", response.decisionInfluence)
            put("helpfulness", response.helpfulness)
            put("frustration", response.frustration)
            put("pause_reconsider", response.pauseReconsider)
            put("easier_to_ignore", response.easierToIgnore)
            put("outside_use_likelihood", response.outsideUseLikelihood)
            
            // Open responses
            put("biggest_influence_aspect", response.biggestInfluenceAspect)
            put("own_words_effect", response.ownWordsEffect)
            put("suggestions", response.suggestions)
        }

        return JSONObject().apply {
            put("device_id", response.deviceId)
            put("sessions", JSONArray()) 
            put("interventions", JSONArray())
            put("exit_survey_response", exitObj)
        }.toString()
    }
}
