package study.doomscrolling.app.domain.intervention

/**
 * Simple in-process notifier used to signal when an intervention overlay finishes.
 * InterventionEngine registers a listener; OverlayService notifies on completion.
 */
object InterventionCompletionNotifier {

    @Volatile
    var listener: ((interventionId: String, sessionId: String, action: String) -> Unit)? = null

    fun notifyCompleted(interventionId: String, sessionId: String, action: String) {
        listener?.invoke(interventionId, sessionId, action)
    }
}
