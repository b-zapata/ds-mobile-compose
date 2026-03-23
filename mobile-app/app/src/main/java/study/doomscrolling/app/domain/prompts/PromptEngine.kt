package study.doomscrolling.app.domain.prompts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import study.doomscrolling.app.data.dao.InterventionDao
import study.doomscrolling.app.data.prompts.PromptRepository
import study.doomscrolling.app.domain.study.StudyArm

data class PromptInstance(
    val text: String,
    val promptVariant: Int
)

/**
 * Selects and renders prompts based on study arm.
 * Implements "least-used" logic to maximize variety and ensure coverage.
 */
class PromptEngine(
    private val promptRepository: PromptRepository,
    private val promptRenderer: PromptRenderer,
    private val interventionDao: InterventionDao
) {

    suspend fun selectPrompt(
        studyArm: StudyArm,
        milestoneMinutes: Int,
        personalization: Map<String, String> = emptyMap()
    ): PromptInstance = withContext(Dispatchers.Default) {
        val armKey = studyArm.name.lowercase()
        val allPrompts = promptRepository.getPrompts()
        val templates = allPrompts[armKey] ?: error("No prompts for arm=$armKey")

        // 1. Get usage counts for all templates in this arm from the DB
        val usedVariants = withContext(Dispatchers.IO) {
            interventionDao.getUsedPromptVariants(armKey)
        }
        
        val usageCounts = mutableMapOf<Int, Int>()
        templates.indices.forEach { index -> usageCounts[index] = 0 }
        usedVariants.forEach { variant ->
            usageCounts[variant] = (usageCounts[variant] ?: 0) + 1
        }

        // 2. Find the minimum usage count among available templates
        val minUsage = usageCounts.values.minOrNull() ?: 0

        // 3. Identify all templates that have been used 'minUsage' times
        val candidates = templates.indices.filter { index -> (usageCounts[index] ?: 0) == minUsage }

        // 4. Pick a random candidate from the least-used pool
        val selectedIndex = candidates.random()
        val template = templates[selectedIndex]

        // 5. Apply Milestone Prefix
        val prefix = when (milestoneMinutes) {
            0 -> ""
            10 -> "10-minute check: "
            15 -> "15-minute check: "
            20 -> "20-minute check (FINAL INTERVENTION): "
            else -> "$milestoneMinutes-minute check: "
        }

        val rendered = promptRenderer.renderPrompt(template, personalization)
        PromptInstance(
            text = "$prefix$rendered",
            promptVariant = selectedIndex
        )
    }
}
