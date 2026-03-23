package study.doomscrolling.app.domain.prompts

import study.doomscrolling.app.domain.study.StudyArm

/**
 * A single intervention prompt.
 */
data class Prompt(
    val id: String,
    val text: String,
    val category: String,
    val arm: StudyArm // Added arm to help UI decide what interaction to show
)
