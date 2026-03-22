package study.doomscrolling.app.domain.prompts

import study.doomscrolling.app.domain.study.StudyArm

/**
 * In-memory prompt library. Will later be replaced with prompts loaded from assets.
 */
class PromptRepository {

    private val prompts = listOf(
        Prompt("prompt_001", "Pause for a moment. Is this helping you feel better right now?", "reflection", StudyArm.CONTROL),
        Prompt("prompt_002", "Take a breath. Do you want to keep scrolling?", "reflection", StudyArm.CONTROL),
        Prompt("prompt_003", "You've been scrolling for a bit. What do you actually want to do right now?", "reflection", StudyArm.CONTROL),
        Prompt("prompt_004", "Is this the best use of your time right now?", "reflection", StudyArm.CONTROL)
    )

    fun getRandomPrompt(): Prompt = prompts.random()
}
