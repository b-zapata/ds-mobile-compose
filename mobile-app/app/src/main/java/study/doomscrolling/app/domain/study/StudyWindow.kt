package study.doomscrolling.app.domain.study

object StudyWindow {
    const val STUDY_DURATION_MS: Long = 15L * 60 * 1000

    fun studyEndAt(enrolledAt: Long?): Long? = enrolledAt?.plus(STUDY_DURATION_MS)

    fun isStudyCompleted(enrolledAt: Long?, nowMs: Long = System.currentTimeMillis()): Boolean {
        val endAt = studyEndAt(enrolledAt) ?: return false
        return nowMs >= endAt
    }
}