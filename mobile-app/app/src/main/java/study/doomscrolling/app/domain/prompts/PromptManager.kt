package study.doomscrolling.app.domain.prompts

import android.content.Context
import android.content.Intent
import android.util.Log
import study.doomscrolling.app.ui.overlay.OverlayPermissionHelper
import study.doomscrolling.app.ui.overlay.OverlayService

/**
 * Selects prompts and displays the overlay UI when an intervention fires.
 */
class PromptManager(
    private val context: Context,
    private val promptRepository: PromptRepository
) {

    fun showPrompt(prompt: Prompt) {
        if (!OverlayPermissionHelper.hasOverlayPermission(context)) {
            Log.w(TAG, "Overlay permission missing")
            return
        }
        val intent = Intent(context, OverlayService::class.java).apply {
            putExtra(OverlayService.EXTRA_PROMPT_ID, prompt.id)
            putExtra(OverlayService.EXTRA_PROMPT_TEXT, prompt.text)
            putExtra(OverlayService.EXTRA_PROMPT_CATEGORY, prompt.category)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        Log.i(TAG, "Prompt overlay displayed")
    }

    companion object {
        private const val TAG = "PromptManager"
    }
}
