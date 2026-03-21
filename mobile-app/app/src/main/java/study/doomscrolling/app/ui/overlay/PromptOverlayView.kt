package study.doomscrolling.app.ui.overlay

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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

/**
 * Minimalistic Black & White overlay designed to destimulate the brain
 * and trigger reflective thinking (System 2).
 */
@Composable
fun PromptOverlayView(
    promptText: String,
    secondsLeft: Int,
    onContinue: () -> Unit,
    onCloseApp: () -> Unit
) {
    val context = LocalContext.current
    val isFinished = secondsLeft <= 0

    // Parse the prompt text to italicize anything inside single quotes
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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
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
                        lineHeight = 36.sp,
                        letterSpacing = 0.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.height(80.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close App Option
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

                    // Continue Option (Timed)
                    if (isFinished) {
                        TextButton(
                            onClick = onContinue,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RectangleShape
                        ) {
                            Text(
                                text = "CONTINUE TO APP",
                                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Normal
                            )
                        }
                    } else {
                        // Static waiting text as requested
                        Text(
                            text = "WAIT 12S TO CONTINUE",
                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
