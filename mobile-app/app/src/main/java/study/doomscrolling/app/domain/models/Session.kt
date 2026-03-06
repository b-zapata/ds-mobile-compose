package study.doomscrolling.app.domain.models

import java.util.UUID

/**
 * In-memory session model for Phase 3.
 * Persistence (e.g. Room) will be added in later phases.
 */
data class Session(
    val sessionId: String,
    val packageName: String,
    val startTimestamp: Long,
    var endTimestamp: Long? = null,
    var durationSeconds: Int = 0
) {
    fun end(endedAt: Long) {
        endTimestamp = endedAt
        durationSeconds = ((endedAt - startTimestamp) / 1000).toInt().coerceAtLeast(0)
    }
}

fun createSession(packageName: String, startTimestamp: Long): Session =
    Session(
        sessionId = UUID.randomUUID().toString(),
        packageName = packageName,
        startTimestamp = startTimestamp
    )
