package study.doomscrolling.app.ui.overlay

import android.content.Intent
import android.os.CountDownTimer
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
                    .padding(40.dp),
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
                    FrictionTaskFactory(promptText = promptText, onTaskComplete = onContinue)
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
        text.contains("TYPE") -> {
            val target = promptText.substringAfter("'").substringBefore("'")
            TypingTask(target = target, onComplete = onTaskComplete)
        }
        text.contains("HOLD") -> {
            val seconds = promptText.filter { it.isDigit() }.toIntOrNull() ?: 10
            HoldingTask(seconds = seconds, onComplete = onTaskComplete)
        }
        text.contains("NUMBERS") -> {
            val isReverse = text.contains("REVERSE")
            SequencingTask(isReverse = isReverse, onComplete = onTaskComplete)
        }
        else -> {
            // Fallback to a simple wait if parsing fails
            StandardTask(isFinished = true, secondsLeft = 0, onContinue = onTaskComplete)
        }
    }
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
            placeholder = { Text("Type here...", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
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
private fun HoldingTask(seconds: Int, onComplete: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }
    val totalMs = seconds * 1000L

    DisposableEffect(isHolding) {
        var timer: CountDownTimer? = null
        if (isHolding) {
            timer = object : CountDownTimer(totalMs, 50) {
                override fun onTick(millisUntilFinished: Long) {
                    progress = (totalMs - millisUntilFinished).toFloat() / totalMs
                }
                override fun onFinish() {
                    progress = 1f
                    onComplete()
                }
            }.start()
        } else {
            progress = 0f
        }
        onDispose { timer?.cancel() }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {}, // Handled via pointerInput
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            isHolding = event.changes.any { it.pressed }
                        }
                    }
                },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isHolding) Color.White else Color.Transparent,
                contentColor = if (isHolding) Color.Black else Color.White
            ),
            border = BorderStroke(1.dp, Color.White)
        ) {
            Text(if (isHolding) "HOLDING..." else "PRESS AND HOLD", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SequencingTask(isReverse: Boolean, onComplete: () -> Unit) {
    val numbers = remember { (1..9).toList().shuffled() }
    var nextTarget by remember { mutableIntStateOf(if (isReverse) 9 else 1) }
    val completed = remember { mutableStateListOf<Int>() }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (isReverse) "TAP 9 TO 1" else "TAP 1 TO 9",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.size(240.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(numbers) { num ->
                val isDone = completed.contains(num)
                OutlinedButton(
                    onClick = {
                        if (num == nextTarget) {
                            completed.add(num)
                            if (isReverse) nextTarget-- else nextTarget++
                            if (completed.size == 9) onComplete()
                        }
                    },
                    modifier = Modifier.aspectRatio(1f),
                    shape = RectangleShape,
                    border = BorderStroke(1.dp, if (isDone) Color.Gray else Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDone) Color.Gray else Color.White
                    ),
                    enabled = !isDone
                ) {
                    Text(text = num.toString(), fontSize = 20.sp)
                }
            }
        }
    }
}
