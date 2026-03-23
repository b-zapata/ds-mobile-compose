package study.doomscrolling.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.data.prompts.PromptRepository
import study.doomscrolling.app.domain.prompts.Prompt
import study.doomscrolling.app.domain.prompts.PromptManager
import study.doomscrolling.app.domain.prompts.PromptRenderer
import study.doomscrolling.app.domain.study.StudyArm
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptTestScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val promptRepository = remember { PromptRepository(context) }
    val promptRenderer = remember { PromptRenderer() }
    val promptManager = remember { PromptManager(context) }
    
    var promptsByArm by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var selectedArm by remember { mutableStateOf("identity") }
    
    LaunchedEffect(Unit) {
        promptsByArm = promptRepository.getPrompts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prompt Debugger") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Arm Selector
            ScrollableTabRow(
                selectedTabIndex = armToIndex(selectedArm),
                edgePadding = 16.dp
            ) {
                arms.forEach { arm ->
                    Tab(
                        selected = selectedArm == arm,
                        onClick = { selectedArm = arm },
                        text = { Text(arm.uppercase()) }
                    )
                }
            }

            // Prompt List
            val currentPrompts = promptsByArm[selectedArm] ?: emptyList()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentPrompts) { template ->
                    Card(
                        onClick = {
                            scope.launch {
                                val device = db.deviceDao().getDevice()
                                val deviceId = device?.deviceId ?: "debug_device"
                                
                                // Get personalization tokens if identity
                                val personalization = if (selectedArm == "identity") {
                                    val response = db.onboardingResponseDao().getOnboardingResponse(deviceId)
                                    if (response != null) {
                                        mapOf(
                                            "Trait 1" to response.trait1,
                                            "Trait 2" to response.trait2,
                                            "Trait 3" to response.trait3,
                                            "Goal 1" to response.goal1,
                                            "Goal 2" to response.goal2,
                                            "Goal 3" to response.goal3,
                                            "Role 1" to response.role1,
                                            "Role 2" to response.role2,
                                            "Role 3" to response.role3
                                        )
                                    } else emptyMap()
                                } else emptyMap()

                                val rendered = promptRenderer.renderPrompt(template, personalization)
                                
                                promptManager.showPrompt(
                                    prompt = Prompt(
                                        id = "debug_${UUID.randomUUID()}",
                                        text = rendered,
                                        category = "debug",
                                        arm = StudyArm.valueOf(selectedArm.uppercase())
                                    ),
                                    interventionId = "debug_${UUID.randomUUID()}",
                                    sessionId = "debug_session"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = template, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

private val arms = listOf("identity", "mindfulness", "friction", "control")
private fun armToIndex(arm: String) = arms.indexOf(arm).coerceAtLeast(0)
