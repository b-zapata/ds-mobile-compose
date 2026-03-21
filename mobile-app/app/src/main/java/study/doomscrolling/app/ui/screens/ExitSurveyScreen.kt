package study.doomscrolling.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import study.doomscrolling.app.viewmodel.ExitSurveyViewModel

@Composable
fun ExitSurveyScreen(
    onComplete: () -> Unit,
    viewModel: ExitSurveyViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Exit Survey",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Thank you for participating in our study. Please complete this final survey to finish.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader("Experience with Interruptions")

        SurveyLikertScale(
            label = "1. During the study, how often did the interruptions make you more aware of how long you had been using an app?",
            value = viewModel.interruptionAwareness,
            onValueChange = { viewModel.interruptionAwareness = it },
            minLabel = "Never",
            maxLabel = "Always"
        )

        SurveyLikertScale(
            label = "2. When the interruption appeared, how often did it influence your decision to stop using the app?",
            value = viewModel.decisionInfluence,
            onValueChange = { viewModel.decisionInfluence = it },
            minLabel = "Never",
            maxLabel = "Always"
        )

        SurveyLikertScale(
            label = "3. Overall, how helpful were the interruptions in helping you reflect on your phone use?",
            value = viewModel.helpfulness,
            onValueChange = { viewModel.helpfulness = it },
            minLabel = "Not helpful",
            maxLabel = "Very helpful"
        )

        SurveyLikertScale(
            label = "4. How frustrating or annoying did you find the interruptions?",
            value = viewModel.frustration,
            onValueChange = { viewModel.frustration = it },
            minLabel = "Not at all",
            maxLabel = "Very much"
        )

        SurveyLikertScale(
            label = "5. Did the interruptions help you pause and reconsider whether you wanted to keep using the app?",
            value = viewModel.pauseReconsider,
            onValueChange = { viewModel.pauseReconsider = it },
            minLabel = "Not at all",
            maxLabel = "Very much"
        )

        SurveyLikertScale(
            label = "6. Over the course of the week, did the interruptions become easier to ignore?",
            value = viewModel.easierToIgnore,
            onValueChange = { viewModel.easierToIgnore = it },
            minLabel = "Much harder",
            maxLabel = "Much easier"
        )

        SurveyLikertScale(
            label = "7. If a tool like this were available on your phone outside of a research study, how likely would you be to use it?",
            value = viewModel.outsideUseLikelihood,
            onValueChange = { viewModel.outsideUseLikelihood = it },
            minLabel = "Not likely",
            maxLabel = "Very likely"
        )

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Your Feedback")

        SurveyOpenResponse(
            label = "8. What aspect of the interruptions had the biggest influence on your decision to continue or stop using the app?",
            value = viewModel.biggestInfluenceAspect,
            onValueChange = { viewModel.biggestInfluenceAspect = it }
        )

        SurveyOpenResponse(
            label = "9. In your own words, how did the interruptions affect your phone use, if at all?",
            value = viewModel.ownWordsEffect,
            onValueChange = { viewModel.ownWordsEffect = it }
        )

        SurveyOpenResponse(
            label = "10. Do you have any suggestions for improving the intervention prompts or tasks?",
            value = viewModel.suggestions,
            onValueChange = { viewModel.suggestions = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.submitSurvey(onComplete) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isSubmitting
        ) {
            Text(if (viewModel.isSubmitting) "Submitting..." else "Submit Survey")
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Divider(modifier = Modifier.padding(bottom = 16.dp))
}

@Composable
private fun SurveyOpenResponse(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
            placeholder = { Text("Your answer here...") }
        )
    }
}

@Composable
private fun SurveyLikertScale(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    minLabel: String,
    maxLabel: String
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = minLabel, style = MaterialTheme.typography.labelSmall)
            Text(text = maxLabel, style = MaterialTheme.typography.labelSmall)
        }
    }
}
