package study.doomscrolling.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.domain.study.DebriefProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebriefScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val device = db.deviceDao().observeDevice().collectAsState(initial = null).value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debrief") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "IRB – Debriefing Statement",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Project Title: Evaluating the Efficacy of Standardized Pattern Interrupts on Mobile Application Persistence",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Thank you for participating in this research study!",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "The purpose of this study was to compare different methods of breaking the \"digital trance\" often associated with social media usage. To ensure the scientific validity of our results, we utilized a technique called incomplete disclosure.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "What was withheld? During the study, you were told that you needed to engage with a specific activity to continue app usage. What we didn't tell you is that it didn't matter how well you engaged with the activity, or whether you engaged with it at all. The app used a secret countdown timer and automatically resumed after 12 seconds no matter what you did.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Why was this necessary? The research team withheld the timer for two primary reasons:",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "1. To prevent \"timer-watching\": If a timer is visible, participants often focus on the countdown rather than authentically engaging with the prompt or task.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "2. Minimalist Design: We aimed to reduce sensory arousal and \"gamification\" by providing as little additional digital stimulation as possible, facilitating a more effective transition out of a habitual scrolling state.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Confidentiality and Data: All data collected during this study remains de-identified and encrypted. If you feel uncomfortable with the use of the hidden timer now that it has been disclosed, you have the right to request that your data be withdrawn from the final analysis.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Contact Information: If you have any questions regarding this study or the debriefing, or if you would like to withdraw your data from the study, please contact Ben Zapata at bz228@byu.edu",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val deviceId = device?.deviceId
                    if (deviceId != null) {
                        DebriefProgress.markCompleted(context, deviceId)
                    }
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I understand")
            }
        }
    }
}
