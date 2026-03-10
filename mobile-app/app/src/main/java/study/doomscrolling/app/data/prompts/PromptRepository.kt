package study.doomscrolling.app.data.prompts

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Loads the prompt library from assets/prompts.json and exposes it as:
 * arm -> milestoneMinutes -> list of prompt templates.
 */
class PromptRepository(
    private val context: Context
) {

    private var cached: Map<String, Map<Int, List<String>>>? = null

    suspend fun getPrompts(): Map<String, Map<Int, List<String>>> =
        cached ?: loadPrompts().also { cached = it }

    private suspend fun loadPrompts(): Map<String, Map<Int, List<String>>> =
        withContext(Dispatchers.IO) {
            val jsonText = context.assets.open(PROMPTS_ASSET_NAME).bufferedReader().use { it.readText() }
            val root = JSONObject(jsonText)
            val result = mutableMapOf<String, MutableMap<Int, MutableList<String>>>()

            val armNames = root.keys()
            while (armNames.hasNext()) {
                val armKey = armNames.next()
                val armObject = root.getJSONObject(armKey)
                val milestones = armObject.keys()
                val milestoneMap = mutableMapOf<Int, MutableList<String>>()
                while (milestones.hasNext()) {
                    val milestoneKey = milestones.next()
                    val minutes = milestoneKey.toIntOrNull() ?: continue
                    val promptsArray = armObject.getJSONArray(milestoneKey)
                    val prompts = mutableListOf<String>()
                    for (i in 0 until promptsArray.length()) {
                        prompts += promptsArray.getString(i)
                    }
                    milestoneMap[minutes] = prompts
                }
                result[armKey.lowercase()] = milestoneMap
            }
            result
        }

    companion object {
        private const val PROMPTS_ASSET_NAME = "prompts.json"
    }
}

