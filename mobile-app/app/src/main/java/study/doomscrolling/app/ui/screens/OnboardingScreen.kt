package study.doomscrolling.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import study.doomscrolling.app.BuildConfig
import study.doomscrolling.app.viewmodel.OnboardingViewModel
import study.doomscrolling.app.viewmodel.OnboardingViewModel.DebugArmOption

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Onboarding",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Placeholder onboarding questionnaire.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (BuildConfig.DEBUG) {
            DebugStudyArmOverrideSection(viewModel = viewModel)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.resetStudyArm() }) {
                Text("Reset Study Arm")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = {
                viewModel.applyStudyArmOverrideIfNeeded()
                onComplete()
            }
        ) {
            Text("Complete")
        }
    }
}

@Composable
private fun DebugStudyArmOverrideSection(
    viewModel: OnboardingViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val current = viewModel.selectedDebugArm
    val options = DebugArmOption.values()

    Text(
        text = "Debug: Study arm override",
        style = MaterialTheme.typography.labelLarge
    )
    Spacer(modifier = Modifier.height(8.dp))

    Button(onClick = { expanded = true }) {
        Text(text = current.label)
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option.label) },
                onClick = {
                    viewModel.onDebugArmSelected(option)
                    expanded = false
                }
            )
        }
    }
}

