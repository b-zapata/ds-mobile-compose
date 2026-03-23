package study.doomscrolling.app.domain.study

import android.content.Context

object DebriefProgress {
    private const val PREF_NAME = "study_progress"
    private const val KEY_PREFIX = "debrief_completed_"

    fun isCompleted(context: Context, deviceId: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("$KEY_PREFIX$deviceId", false)
    }

    fun markCompleted(context: Context, deviceId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("$KEY_PREFIX$deviceId", true).apply()
    }
}
