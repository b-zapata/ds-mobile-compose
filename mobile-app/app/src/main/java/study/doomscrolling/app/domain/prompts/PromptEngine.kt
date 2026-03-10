package study.doomscrolling.app.domain.prompts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import study.doomscrolling.app.data.prompts.PromptRepository
import study.doomscrolling.app.domain.study.StudyArm

data class PromptInstance(
    val text: String,
    val promptVariant: Int
)

/**
 * Selects and renders prompts based on study arm and milestone.
 */
class PromptEngine(
    private val promptRepository: PromptRepository,
    private val promptRenderer: PromptRenderer
) {

    suspend fun selectPrompt(
        studyArm: StudyArm,
        milestoneMinutes: Int,
        personalization: Map<String, String> = emptyMap()
    ): PromptInstance = withContext(Dispatchers.Default) {
        val allPrompts = promptRepository.getPrompts()
        val armKey = studyArm.name.lowercase()
        val milestones = allPrompts[armKey]
            ?: error("No prompts configured for arm=$armKey")
        val templates = milestones[milestoneMinutes]
            ?: error("No prompts configured for arm=$armKey milestone=$milestoneMinutes")
        if (templates.isEmpty()) error("Empty prompt list for arm=$armKey milestone=$milestoneMinutes")

        val index = (templates.indices).random()
        val template = templates[index]
        val rendered = promptRenderer.renderPrompt(template, personalization)
        PromptInstance(
            text = rendered,
            promptVariant = index
        )
    }
}

