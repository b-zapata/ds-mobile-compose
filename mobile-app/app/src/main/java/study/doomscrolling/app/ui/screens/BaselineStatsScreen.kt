package study.doomscrolling.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import study.doomscrolling.app.viewmodel.BaselineStatsViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun BaselineStatsScreen(
    viewModel: BaselineStatsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDayFilter by remember { mutableStateOf<String?>(null) } // null = full week

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Baseline Statistics",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Last 7 days (debug view)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.loading && uiState.stats == null) {
            Text("Loading...")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.refresh() }) {
                Text("Refresh")
            }
            return
        }

        val stats = uiState.stats
        if (stats == null) {
            Text("No baseline data available yet.")
            Text(
                text = "Complete onboarding to import, or tap below to import now (last 7 days).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.importBaselineNow() }) {
                    Text("Import baseline now")
                }
                Button(onClick = { viewModel.refresh() }) {
                    Text("Refresh")
                }
            }
            return
        }

        val totalTimeFormatted = formatDuration(stats.totalDurationSeconds)
        val avgSessionFormatted = formatDuration(stats.avgSessionSeconds.toLong())

        // Summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Summary", style = MaterialTheme.typography.titleMedium)
                Text("Total Sessions: ${stats.totalSessions}")
                Text("Total Time: $totalTimeFormatted")
                Text("Average Session: $avgSessionFormatted")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable detail sections
        val zoneId = remember { ZoneId.systemDefault() }
        val dayFormatter = remember { DateTimeFormatter.ofPattern("EEE") }
        val today = remember { LocalDate.now(zoneId) }
        val allDayLabels = remember {
            (0L until 7L).map { offset ->
                dayFormatter.format(today.minusDays(offset))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f, fill = true)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter row: Week vs per-day
            FilterRow(
                days = allDayLabels,
                selectedDay = selectedDayFilter,
                onSelected = { selectedDayFilter = it }
            )

            Text(
                text = "Most Used Apps",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (stats.usageByApp.isEmpty()) {
                Text(
                    text = "No app usage recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val usageList = selectedDayFilter?.let { day ->
                    stats.usageByAppByDay[day].orEmpty()
                } ?: stats.usageByApp

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    usageList.forEach { appStat ->
                        Text(
                            text = "${appStat.appPackage} — ${formatDuration(appStat.totalSeconds)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Divider()

            Text(
                text = "Sessions Per Day",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (stats.sessionsPerDay.isEmpty()) {
                Text(
                    text = "No sessions recorded in the last 7 days.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stats.sessionsPerDay.forEach { dayStat ->
                        Text(
                            text = "${dayStat.date} — ${dayStat.sessionCount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = { viewModel.refresh() }
        ) {
            Text("Refresh")
        }
    }
}

@Composable
private fun FilterRow(
    days: List<String>,
    selectedDay: String?,
    onSelected: (String?) -> Unit
) {
    if (days.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onSelected(null) },
            enabled = selectedDay != null
        ) {
            Text("Full Week")
        }
        days.forEach { dayLabel ->
            Button(
                onClick = { onSelected(dayLabel) },
                enabled = selectedDay != dayLabel
            ) {
                Text(dayLabel)
            }
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    if (totalSeconds <= 0L) return "0s"
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m ")
        if (hours == 0L && minutes == 0L && seconds > 0) append("${seconds}s")
    }.trim()
}

