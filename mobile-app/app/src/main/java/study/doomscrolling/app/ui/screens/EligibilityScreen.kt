package study.doomscrolling.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import study.doomscrolling.app.viewmodel.EligibilityViewModel

@Composable
fun EligibilityScreen(
    onComplete: () -> Unit,
    viewModel: EligibilityViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Eligibility Check",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "We will now check if you are eligible for this study based on your phone usage over the last 7 days. This requires usage access permission.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (state.checking) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Analyzing usage data and connecting to server...")
        } else {
            Button(
                onClick = { viewModel.checkEligibilityAndUploadBaseline() }
            ) {
                Text("Check Eligibility")
            }
        }

        state.message?.let { msg ->
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = msg,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (state.eligible == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }

        if (state.eligible == true && !state.checking) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onComplete) {
                Text("Continue")
            }
        }
    }
}
