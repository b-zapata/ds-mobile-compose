package study.doomscrolling.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import study.doomscrolling.app.viewmodel.ConsentViewModel

@Composable
fun ConsentScreen(
    onAccept: () -> Unit,
    viewModel: ConsentViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Implied Consent",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Evaluating the Comparative Efficacy of Identity-Based, Mindfulness-Based, and Friction-Based Interventions in Reducing Habitual Mobile Application Usage.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = """
Researcher Information

My name is Ben Zapata, and I am a student at Brigham Young University conducting this research under the supervision of Professor James Gaskin from the Department of Information Systems. You are invited to participate in a research study examining how brief interruptions during social media use may influence people’s smartphone usage habits.
Participation in this study is voluntary.

Purpose of the Study

The purpose of this study is to evaluate how short interruptions during social media use affect how people interact with mobile apps. The research aims to better understand whether brief prompts or tasks can help people pause and reflect on their phone usage and decide whether they want continue using an app.

What You Will Be Asked to Do

If you choose to participate in this study, you will be asked to:
- Install a research application on your Android smartphone.
- Complete a brief onboarding activity within the app. This may include answering a short questionnaire or completing a short calibration activity.
- Use your phone normally for approximately 7 days while the research application runs in the background.
- At the end of the 7-day study period, you will also be asked to complete a short exit survey within the app (approximately 2–3 minutes).

The research application monitors usage of certain social media apps, including:
- YouTube
- Instagram
- Threads
- Facebook
- TikTok
- X (formerly Twitter)
- Reddit

During the study period, the research application may temporarily interrupt the use of these apps after extended use (for example, after approximately 10–15 minutes of continuous use). When this happens, a brief prompt or activity will appear on your screen. These interruptions last approximately 12 seconds, after which normal app usage can resume.

The research application also collects limited information about app usage, such as:
- Which apps were used
- When apps were opened and closed
- How long the apps were used

The app does not collect messages, photos, videos, notifications, or other personal content from your phone.

Time Commitment

- Initial setup: approximately 5–10 minutes
- Study participation: 7 days of normal phone use
- Interventions: brief interruptions lasting approximately 12 seconds during some app sessions

Possible Risks

The risks associated with this study are minimal. Participants may experience mild inconvenience or brief frustration when app usage is temporarily interrupted. Some participants may also experience mild self-awareness or reflection regarding their technology habits.

There is also a small risk related to the collection of app usage data. However, the data collected does not include personal messages or content, and steps are taken to protect participant privacy. These risks are no greater than those commonly experienced during normal use of digital well-being applications.

Privacy and Confidentiality

The research application only collects limited app usage metadata (such as app names and usage times). No personal content, messages, images, or account information are collected.

Data are associated with a randomly generated device identifier and are analyzed in aggregate. The research team does not collect names, email addresses, phone numbers, or other direct identifiers.

Stopping Participation

Participation in this study is voluntary. You may stop participating at any time without penalty.

To stop participation and stop the research application from collecting data, you may uninstall the app from your phone:
- Open the Settings app on your Android device.
- Tap Apps (or Apps & Notifications, depending on your device).
- Locate the research application in the app list.
- Tap the app name.
- Tap Uninstall.

Once the application is uninstalled, it will immediately stop monitoring app usage and no additional data will be collected. You may also stop the study by disabling the app’s permissions through your phone’s settings.

Compensation

Participants will receive 4 SONA research credits upon completing the study.

Questions

If you have questions about this study, please contact:
- Ben Zapata — bz228@byu.edu

If you have questions about your rights as a research participant, you may contact the BYU Human Research Protection Program at:
- Phone: 801-422-1461
- Email: BYU.HRPP@byu.edu

Consent

If you would like to participate in this study, please select “Accept” to continue.
""".trimIndent(),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.acceptConsent(onAccept) }, 
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text("Accept")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
