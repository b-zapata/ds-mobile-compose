package study.doomscrolling.app.domain.prompts

/**
 * In-memory prompt library. Will later be replaced with prompts loaded from assets.
 */
class PromptRepository {

    private val prompts = listOf(
        Prompt("prompt_001", "Pause for a moment. Is this helping you feel better right now?", "reflection"),
        Prompt("prompt_002", "Take a breath. Do you want to keep scrolling?", "reflection"),
        Prompt("prompt_003", "You've been scrolling for a bit. What do you actually want to do right now?", "reflection"),
        Prompt("prompt_004", "Is this the best use of your time right now?", "reflection")
    )

    fun getRandomPrompt(): Prompt = prompts.random()
}
