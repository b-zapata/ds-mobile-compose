package study.doomscrolling.app.domain.prompts

/**
 * Renders a prompt template by replacing personalization tokens like [Trait 1]
 * using values from the provided map.
 */
class PromptRenderer {

    private val tokenRegex = Regex("\\[(.+?)]")

    fun renderPrompt(
        template: String,
        personalization: Map<String, String>
    ): String {
        if (personalization.isEmpty()) return template
        return tokenRegex.replace(template) { matchResult ->
            val key = matchResult.groupValues.getOrNull(1)?.trim().orEmpty()
            personalization[key]?.ifBlank { null } ?: matchResult.value
        }
    }
}

