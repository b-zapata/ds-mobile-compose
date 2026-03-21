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
import study.doomscrolling.app.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val totalSteps = 12
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
                    Text(
                        text = "Question ${currentStep + 1} of $totalSteps",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                            onClick = { viewModel.submitOnboarding(onComplete) },
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
                        0 -> OnboardingStep(
                            question = "What is the #1 character trait of your 'Best Self'?",
                            placeholder = "e.g., Disciplined, Brave",
                            value = viewModel.trait1,
                            onValueChange = viewModel::onTrait1Change
                        )
                        1 -> OnboardingStep(
                            question = "What quality do you admire most in people you look up to?",
                            placeholder = "e.g., Focus, Consistency",
                            value = viewModel.trait2,
                            onValueChange = viewModel::onTrait2Change
                        )
                        2 -> OnboardingStep(
                            question = "What is one trait you are actively trying to develop this year?",
                            placeholder = "e.g., Patience, Presence",
                            value = viewModel.trait3,
                            onValueChange = viewModel::onTrait3Change
                        )
                        3 -> OnboardingStep(
                            question = "What is the most important academic or professional goal you’re working toward right now?",
                            value = viewModel.goal1,
                            onValueChange = viewModel::onGoal1Change
                        )
                        4 -> OnboardingStep(
                            question = "What is a personal passion or skill you want to improve?",
                            placeholder = "e.g., Guitar, Photography",
                            value = viewModel.goal2,
                            onValueChange = viewModel::onGoal2Change
                        )
                        5 -> OnboardingStep(
                            question = "What is a long-term dream you hope to achieve?",
                            value = viewModel.goal3,
                            onValueChange = viewModel::onGoal3Change
                        )
                        6 -> OnboardingStep(
                            question = "What is a role you have where someone depends on you?",
                            placeholder = "e.g., mentor, older sibling, best friend",
                            value = viewModel.role1,
                            onValueChange = viewModel::onRole1Change
                        )
                        7 -> OnboardingStep(
                            question = "How would you describe your primary role in the world right now?",
                            placeholder = "e.g., student, creator, aspiring engineer",
                            value = viewModel.role2,
                            onValueChange = viewModel::onRole2Change
                        )
                        8 -> OnboardingStep(
                            question = "What is a role or identity you want to live up to?",
                            value = viewModel.role3,
                            onValueChange = viewModel::onRole3Change
                        )
                        9 -> LikertStep(
                            question = "How often do you open your most-used app without thinking about it?",
                            value = viewModel.automaticity,
                            onValueChange = viewModel::onAutomaticityChange,
                            minLabel = "Not often at all",
                            maxLabel = "Very often"
                        )
                        10 -> LikertStep(
                            question = "After spending time scrolling, how do you usually feel?",
                            value = viewModel.utility,
                            onValueChange = viewModel::onUtilityChange,
                            minLabel = "Much worse",
                            maxLabel = "Much better"
                        )
                        11 -> LikertStep(
                            question = "To what extent do you want to reduce the amount of time you spend scrolling?",
                            value = viewModel.intention,
                            onValueChange = viewModel::onIntentionChange,
                            minLabel = "Not at all",
                            maxLabel = "Very much"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStep(
    question: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
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
            placeholder = { 
                Text(
                    text = placeholder,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                ) 
            },
            modifier = Modifier.fillMaxWidth(),
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

private fun isStepValid(step: Int, viewModel: OnboardingViewModel): Boolean {
    return when (step) {
        0 -> viewModel.trait1.isNotBlank()
        1 -> viewModel.trait2.isNotBlank()
        2 -> viewModel.trait3.isNotBlank()
        3 -> viewModel.goal1.isNotBlank()
        4 -> viewModel.goal2.isNotBlank()
        5 -> viewModel.goal3.isNotBlank()
        6 -> viewModel.role1.isNotBlank()
        7 -> viewModel.role2.isNotBlank()
        8 -> viewModel.role3.isNotBlank()
        else -> true // Likert scales are always valid as they have a default value
    }
}
