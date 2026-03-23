package study.doomscrolling.app.ui.overlay

import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.doomscrolling.app.domain.study.StudyArm
import kotlin.random.Random

@Composable
fun PromptOverlayView(
    promptText: String,
    secondsLeft: Int,
    studyArm: StudyArm,
    onContinue: () -> Unit,
    onCloseApp: () -> Unit
) {
    val context = LocalContext.current
    val isTimerFinished = secondsLeft <= 0

    // Parse styled text (italics for quotes)
    val styledPrompt = remember(promptText) {
        buildAnnotatedString {
            val parts = promptText.split("'")
            parts.forEachIndexed { index, part ->
                if (index % 2 != 0) {
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("'$part'")
                    }
                } else {
                    append(part)
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color.White,
            onPrimary = Color.Black,
            background = Color.Black,
            onBackground = Color.White,
            surface = Color.Black,
            onSurface = Color.White,
            outline = Color.White
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 40.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = styledPrompt,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 36.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Mode-based interaction
                if (studyArm == StudyArm.FRICTION) {
                    FrictionTaskFactory(
                        promptText = promptText, 
                        onTaskComplete = onContinue
                    )
                } else {
                    StandardTask(
                        isFinished = isTimerFinished,
                        secondsLeft = secondsLeft,
                        onContinue = onContinue
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Close App is ALWAYS available as the primary alternative
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                        onCloseApp()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RectangleShape,
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = "CLOSE APP",
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardTask(
    isFinished: Boolean,
    secondsLeft: Int,
    onContinue: () -> Unit
) {
    if (isFinished) {
        TextButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RectangleShape
        ) {
            Text(
                text = "CONTINUE TO APP",
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    } else {
        Text(
            text = "WAIT 12S TO CONTINUE",
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}

/**
 * Parses the prompt text to determine which boring friction task to show.
 */
@Composable
private fun FrictionTaskFactory(promptText: String, onTaskComplete: () -> Unit) {
    val text = promptText.uppercase()
    when {
        // 1. Sequence Tap Task: "Tap the sequence '...' backwards"
        text.contains("SEQUENCE") && text.contains("BACKWARDS") && text.contains("TAP") -> {
            val sequence = promptText.substringAfter("'").substringBefore("'")
            val targetDigits = sequence.filter { it.isDigit() }.reversed()
            SequencingTask(customSequence = targetDigits, onComplete = onTaskComplete)
        }
        // 2. Backwards Typing: "Type the word '...' backwards"
        text.contains("BACKWARDS") -> {
            val word = promptText.substringAfter("'").substringBefore("'")
            val target = word.reversed()
            Log.d("Friction", "Reverse Task -> Target: $target")
            TypingTask(target = target, onComplete = onTaskComplete)
        }
        // 3. Math Task: "Type the answer to '...'"
        text.contains("ANSWER") -> {
            val target = calculateMathAnswer(promptText)
            Log.d("Friction", "Math Task -> Target: $target")
            TypingTask(target = target, onComplete = onTaskComplete)
        }
        // 4. Sequencing Task: "Tap the numbers..."
        text.contains("NUMBERS") -> {
            val isReverse = text.contains("REVERSE")
            Log.d("Friction", "Sequence Task -> Reverse: $isReverse")
            SequencingTask(customSequence = if (isReverse) "987654321" else "123456789", onComplete = onTaskComplete)
        }
        // 5. Default Typing: "Type the code '...'" or "Type the phrase '...'"
        text.contains("TYPE") -> {
            val target = promptText.substringAfter("'").substringBefore("'")
            Log.d("Friction", "Direct Type Task -> Target: $target")
            TypingTask(target = target, onComplete = onTaskComplete)
        }
        else -> {
            StandardTask(isFinished = true, secondsLeft = 0, onContinue = onTaskComplete)
        }
    }
}

private fun calculateMathAnswer(prompt: String): String {
    val mathExpression = prompt.substringAfter("'").substringBefore("'")
    val numbers = Regex("(\\d+)").findAll(mathExpression).map { it.value.toInt() }.toList()
    
    if (numbers.size < 2) return "0"
    
    val result = when {
        mathExpression.contains("+") -> numbers[0] + numbers[1]
        mathExpression.contains("-") -> numbers[0] - numbers[1]
        mathExpression.uppercase().contains("TIMES") -> numbers[0] * numbers[1]
        else -> 0
    }
    return result.toString()
}

@Composable
private fun TypingTask(target: String, onComplete: () -> Unit) {
    var input by remember { mutableStateOf("") }
    val isMatch = input.trim().equals(target, ignoreCase = true)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, color = Color.White),
            placeholder = { Text("Type answer...", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                cursorColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (isMatch) {
            TextButton(onClick = onComplete, modifier = Modifier.fillMaxWidth(), shape = RectangleShape) {
                Text("CONTINUE", color = Color.White, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
private fun SequencingTask(customSequence: String, onComplete: () -> Unit) {
    val targetDigits = customSequence.map { it.toString() }
    val gridDigits = remember(customSequence) { 
        (0..9).map { it.toString() }.shuffled() 
    }
    
    var currentStep by remember { mutableIntStateOf(0) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Research-backed change: Don't reveal the target sequence characters after the current step.
        // The user must actually remember/calculate the backward sequence from the prompt text.
        Text(
            text = "TARGET: ${customSequence.mapIndexed { i, _ -> if (i < currentStep) "✓" else "?" }.joinToString(" ")}",
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.width(240.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gridDigits) { digit ->
                val isNext = currentStep < targetDigits.size && digit == targetDigits[currentStep]
                
                OutlinedButton(
                    onClick = {
                        if (isNext) {
                            currentStep++
                            if (currentStep == targetDigits.size) onComplete()
                        } else {
                            // Penalty: Reset on wrong tap
                            currentStep = 0
                        }
                    },
                    modifier = Modifier.aspectRatio(1f),
                    shape = RectangleShape,
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(text = digit, fontSize = 20.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Step $currentStep of ${targetDigits.size}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}
