package study.doomscrolling.app.data.prompts

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Loads the prompt library from assets/prompts.json and exposes it as:
 * arm -> list of prompt templates.
 */
class PromptRepository(
    private val context: Context
) {

    private var cached: Map<String, List<String>>? = null

    suspend fun getPrompts(): Map<String, List<String>> =
        cached ?: loadPrompts().also { cached = it }

    private suspend fun loadPrompts(): Map<String, List<String>> =
        withContext(Dispatchers.IO) {
            val jsonText = context.assets.open(PROMPTS_ASSET_NAME).bufferedReader().use { it.readText() }
            val root = JSONObject(jsonText)
            val result = mutableMapOf<String, List<String>>()

            val armNames = root.keys()
            while (armNames.hasNext()) {
                val armKey = armNames.next()
                val promptsArray = root.getJSONArray(armKey)
                val prompts = mutableListOf<String>()
                for (i in 0 until promptsArray.length()) {
                    prompts += promptsArray.getString(i)
                }
                result[armKey.lowercase()] = prompts
            }
            result
        }

    companion object {
        private const val PROMPTS_ASSET_NAME = "prompts.json"
    }
}
