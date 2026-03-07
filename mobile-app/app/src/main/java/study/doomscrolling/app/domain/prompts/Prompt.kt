package study.doomscrolling.app.domain.prompts

/**
 * A single intervention prompt. Will later be loaded from assets (prompts.json).
 */
data class Prompt(
    val id: String,
    val text: String,
    val category: String
)
