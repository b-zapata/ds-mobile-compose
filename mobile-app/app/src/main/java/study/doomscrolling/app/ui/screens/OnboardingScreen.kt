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
import study.doomscrolling.app.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
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
            text = "Onboarding Survey",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please answer the following questions to help personalize your experience.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Identity Questions (Arm A)
        SectionHeader("Identity & Values")
        
        SurveyTextField(
            label = "1. What is the #1 character trait of your 'Best Self'?",
            value = viewModel.trait1,
            onValueChange = viewModel::onTrait1Change,
            placeholder = "e.g., Disciplined, Brave"
        )
        SurveyTextField(
            label = "2. What quality do you admire most in people you look up to?",
            value = viewModel.trait2,
            onValueChange = viewModel::onTrait2Change,
            placeholder = "e.g., Focus, Consistency"
        )
        SurveyTextField(
            label = "3. What is one trait you are actively trying to develop this year?",
            value = viewModel.trait3,
            onValueChange = viewModel::onTrait3Change,
            placeholder = "e.g., Patience, Presence"
        )
        SurveyTextField(
            label = "4. What is the most important academic or professional goal you’re working toward right now?",
            value = viewModel.goal1,
            onValueChange = viewModel::onGoal1Change
        )
        SurveyTextField(
            label = "5. What is a personal passion or skill you want to improve?",
            value = viewModel.goal2,
            onValueChange = viewModel::onGoal2Change,
            placeholder = "e.g., Guitar, Photography"
        )
        SurveyTextField(
            label = "6. What is a long-term dream you hope to achieve?",
            value = viewModel.goal3,
            onValueChange = viewModel::onGoal3Change
        )
        SurveyTextField(
            label = "7. What is a role you have where someone depends on you?",
            value = viewModel.role1,
            onValueChange = viewModel::onRole1Change,
            placeholder = "e.g., mentor, older sibling, best friend"
        )
        SurveyTextField(
            label = "8. How would you describe your primary role in the world right now?",
            value = viewModel.role2,
            onValueChange = viewModel::onRole2Change,
            placeholder = "e.g., student, creator, aspiring engineer"
        )
        SurveyTextField(
            label = "9. What is a role or identity you want to live up to?",
            value = viewModel.role3,
            onValueChange = viewModel::onRole3Change
        )

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Phone Usage Habits")

        SurveyLikertScale(
            label = "10. How often do you open your most-used app without thinking about it?",
            value = viewModel.automaticity,
            onValueChange = viewModel::onAutomaticityChange,
            minLabel = "Not often",
            maxLabel = "Very often"
        )
        SurveyLikertScale(
            label = "11. After spending time scrolling, how do you usually feel?",
            value = viewModel.utility,
            onValueChange = viewModel::onUtilityChange,
            minLabel = "Much worse",
            maxLabel = "Much better"
        )
        SurveyLikertScale(
            label = "12. To what extent do you want to reduce the amount of time you spend scrolling?",
            value = viewModel.intention,
            onValueChange = viewModel::onIntentionChange,
            minLabel = "Not at all",
            maxLabel = "Very much"
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.submitOnboarding(onComplete) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isSubmitting
        ) {
            Text(if (viewModel.isSubmitting) "Submitting..." else "Complete Onboarding")
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
private fun SurveyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = true
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
