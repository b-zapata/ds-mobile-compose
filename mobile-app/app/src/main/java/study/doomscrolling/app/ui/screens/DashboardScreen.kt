package study.doomscrolling.app.ui.screens

import android.provider.Settings
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.collectLatest
import study.doomscrolling.app.BuildConfig
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.entities.ExitSurveyResponseEntity
import study.doomscrolling.app.data.entities.OnboardingResponseEntity
import study.doomscrolling.app.services.ForegroundAppDetector
import study.doomscrolling.app.workers.UploadWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToConsent: () -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToEligibility: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToExitSurvey: () -> Unit = {},
    onOpenBaselineStats: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    
    // States for the checklist items observed as Flow
    val device by db.deviceDao().observeDevice().collectAsState(initial = null)
    
    val hasConsented = device != null
    val isEligible = device?.enrolledAt != null
    
    // For permissions we still need a side-effect because they aren't in the DB
    var hasPermissions by remember { mutableStateOf(false) }
    
    // Check onboarding from the table
    var onboarding by remember { mutableStateOf<OnboardingResponseEntity?>(null) }
    LaunchedEffect(device?.deviceId) {
        val deviceId = device?.deviceId
        if (deviceId != null) {
            db.onboardingResponseDao().observeOnboardingResponse(deviceId).collectLatest {
                onboarding = it
            }
        }
    }
    val hasOnboarded = onboarding != null

    // Check exit survey from the table
    var exitSurvey by remember { mutableStateOf<ExitSurveyResponseEntity?>(null) }
    LaunchedEffect(device?.deviceId) {
        val deviceId = device?.deviceId
        if (deviceId != null) {
            db.exitSurveyResponseDao().observeExitSurveyResponse(deviceId).collectLatest {
                exitSurvey = it
            }
        }
    }
    val hasExited = exitSurvey != null
    
    var timeRemainingText by remember { mutableStateOf<String?>(null) }
    var studyCompleted by remember { mutableStateOf(false) }

    // Refresh states from System and handle countdown
    LaunchedEffect(device) {
        hasPermissions = ForegroundAppDetector.hasUsageStatsPermission(context) &&
                Settings.canDrawOverlays(context)
        
        val enrolledAt = device?.enrolledAt
        if (enrolledAt != null) {
            val weekMillis = 7L * 24 * 60 * 60 * 1000
            val now = System.currentTimeMillis()
            val endAt = enrolledAt + weekMillis
            studyCompleted = now >= endAt
            
            if (!studyCompleted) {
                val diff = endAt - now
                val days = diff / (24 * 60 * 60 * 1000)
                val hours = (diff / (60 * 60 * 1000)) % 24
                timeRemainingText = "$days days, $hours hours remaining"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Checklist", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome to the study. Please complete the tasks below to help us with our research.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ChecklistItem(
                title = "Sign consent form",
                isCompleted = hasConsented,
                onClick = if (!hasConsented) onNavigateToConsent else null
            )
            ChecklistItem(
                title = "Grant phone permissions",
                isCompleted = hasPermissions,
                onClick = if (hasConsented && !hasPermissions) onNavigateToPermissions else null
            )
            ChecklistItem(
                title = "Check eligibility",
                isCompleted = isEligible,
                onClick = if (hasPermissions && !isEligible) onNavigateToEligibility else null
            )
            ChecklistItem(
                title = "Fill out onboarding survey",
                isCompleted = hasOnboarded,
                onClick = if (isEligible && !hasOnboarded) onNavigateToOnboarding else null
            )
            ChecklistItem(
                title = "Use your phone normally for a week",
                isCompleted = studyCompleted,
                subtitle = if (isEligible && !studyCompleted) timeRemainingText ?: "Study in progress" else null
            )
            ChecklistItem(
                title = "Fill out exit survey",
                isCompleted = hasExited,
                subtitle = if (studyCompleted && !hasExited) "Ready to complete" else null,
                onClick = if (studyCompleted && !hasExited) onNavigateToExitSurvey else null
            )

            if (BuildConfig.DEBUG) {
                Divider()
                Text("Debug Actions", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onOpenBaselineStats, modifier = Modifier.weight(1f)) {
                        Text("Stats")
                    }
                    Button(
                        onClick = {
                            val workManager = WorkManager.getInstance(context)
                            val request = OneTimeWorkRequestBuilder<UploadWorker>().build()
                            workManager.enqueue(request)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Upload")
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistItem(
    title: String,
    isCompleted: Boolean,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCompleted) FontWeight.Medium else FontWeight.Normal,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
