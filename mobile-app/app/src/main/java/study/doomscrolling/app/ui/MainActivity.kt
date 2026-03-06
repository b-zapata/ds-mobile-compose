package study.doomscrolling.app.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import study.doomscrolling.app.services.ForegroundAppDetector
import study.doomscrolling.app.services.UsageTrackingService
import study.doomscrolling.app.ui.navigation.AppNavHost
import study.doomscrolling.app.ui.theme.DoomscrollingStudyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startUsageTrackingServiceIfPermitted()
        setContent {
            DoomscrollingStudyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(navController = navController)
                }
            }
        }
    }

    private fun startUsageTrackingServiceIfPermitted() {
        if (!ForegroundAppDetector.hasUsageStatsPermission(this)) return
        val intent = Intent(this, UsageTrackingService::class.java).apply {
            action = UsageTrackingService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
