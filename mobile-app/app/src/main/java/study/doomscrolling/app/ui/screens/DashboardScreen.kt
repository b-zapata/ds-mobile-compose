package study.doomscrolling.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import study.doomscrolling.app.BuildConfig

@Composable
fun DashboardScreen(
    onOpenBaselineStats: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Placeholder study dashboard.",
            style = MaterialTheme.typography.bodyMedium
        )
        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onOpenBaselineStats) {
                Text("Baseline Stats (Debug)")
            }
        }
    }
}
