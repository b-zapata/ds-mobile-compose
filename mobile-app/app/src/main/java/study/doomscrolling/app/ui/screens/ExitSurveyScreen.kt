package study.doomscrolling.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import study.doomscrolling.app.viewmodel.ExitSurveyViewModel

@Composable
fun ExitSurveyScreen(
    onComplete: () -> Unit,
    viewModel: ExitSurveyViewModel = viewModel()
) {
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val totalSteps = 10
    val progress = (currentStep + 1).toFloat() / totalSteps.toFloat()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                    
                    val isPart1 = currentStep < 7
                    val partLabel = if (isPart1) "Part 1 of 2: Experience" else "Part 2 of 2: Feedback"
                    val relativeStep = if (isPart1) currentStep + 1 else currentStep - 6
                    val relativeTotal = if (isPart1) 7 else 3

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = partLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Question $relativeStep of $relativeTotal",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (currentStep < totalSteps - 1) {
                        Button(
                            onClick = { currentStep++ },
                            enabled = isStepValid(currentStep, viewModel)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.submitSurvey(onComplete) },
                            enabled = !viewModel.isSubmitting && isStepValid(currentStep, viewModel)
                        ) {
                            if (viewModel.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Complete")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "StepContent"
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (step) {
                        0 -> LikertStep(
                            question = "During the study, how often did the interruptions make you more aware of how long you had been using an app?",
                            value = viewModel.interruptionAwareness,
                            onValueChange = { viewModel.interruptionAwareness = it },
                            minLabel = "Never",
                            maxLabel = "Always"
                        )
                        1 -> LikertStep(
                            question = "When the interruption appeared, how often did it influence your decision to stop using the app?",
                            value = viewModel.decisionInfluence,
                            onValueChange = { viewModel.decisionInfluence = it },
                            minLabel = "Never",
                            maxLabel = "Always"
                        )
                        2 -> LikertStep(
                            question = "Overall, how helpful were the interruptions in helping you reflect on your phone use?",
                            value = viewModel.helpfulness,
                            onValueChange = { viewModel.helpfulness = it },
                            minLabel = "Not helpful",
                            maxLabel = "Very helpful"
                        )
                        3 -> LikertStep(
                            question = "How frustrating or annoying did you find the interruptions?",
                            value = viewModel.frustration,
                            onValueChange = { viewModel.frustration = it },
                            minLabel = "Not at all",
                            maxLabel = "Very much"
                        )
                        4 -> LikertStep(
                            question = "Did the interruptions help you pause and reconsider whether you wanted to keep using the app?",
                            value = viewModel.pauseReconsider,
                            onValueChange = { viewModel.pauseReconsider = it },
                            minLabel = "Not at all",
                            maxLabel = "Very much"
                        )
                        5 -> LikertStep(
                            question = "Over the course of the week, did the interruptions become easier to ignore?",
                            value = viewModel.easierToIgnore,
                            onValueChange = { viewModel.easierToIgnore = it },
                            minLabel = "Much harder",
                            maxLabel = "Much easier"
                        )
                        6 -> LikertStep(
                            question = "If a tool like this were available on your phone outside of a research study for free, how likely would you be to use it?",
                            value = viewModel.outsideUseLikelihood,
                            onValueChange = { viewModel.outsideUseLikelihood = it },
                            minLabel = "Not likely",
                            maxLabel = "Very likely"
                        )
                        7 -> OpenResponseStep(
                            question = "What aspect of the interruptions had the biggest influence on your decision to continue or stop using the app?",
                            value = viewModel.biggestInfluenceAspect,
                            onValueChange = { viewModel.biggestInfluenceAspect = it }
                        )
                        8 -> OpenResponseStep(
                            question = "In your own words, how did the interruptions affect your phone use, if at all?",
                            value = viewModel.ownWordsEffect,
                            onValueChange = { viewModel.ownWordsEffect = it }
                        )
                        9 -> OpenResponseStep(
                            question = "Do you have any suggestions for improving the intervention prompts or tasks?",
                            value = viewModel.suggestions,
                            onValueChange = { viewModel.suggestions = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenResponseStep(
    question: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
            placeholder = { 
                Text(
                    text = "Your answer here...",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                ) 
            },
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
private fun LikertStep(
    question: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    minLabel: String,
    maxLabel: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(48.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = minLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                text = maxLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Rating: $value",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun isStepValid(step: Int, viewModel: ExitSurveyViewModel): Boolean {
    return when (step) {
        7 -> viewModel.biggestInfluenceAspect.isNotBlank()
        8 -> viewModel.ownWordsEffect.isNotBlank()
        9 -> viewModel.suggestions.isNotBlank()
        else -> true // Likert scales are always valid as they have a default value
    }
}
