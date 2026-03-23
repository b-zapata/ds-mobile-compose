package study.doomscrolling.app.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.runBlocking
import study.doomscrolling.app.data.database.AppDatabase
import study.doomscrolling.app.domain.study.StudyWindow
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
        val enrolledAt = runBlocking {
            AppDatabase.getInstance(applicationContext).deviceDao().getDevice()?.enrolledAt
        }
        val endAt = StudyWindow.studyEndAt(enrolledAt)
        val isStudyCompleted = StudyWindow.isStudyCompleted(enrolledAt)
        Log.i(TAG, "Service gate: enrolledAt=$enrolledAt endAt=$endAt now=${System.currentTimeMillis()} studyCompleted=$isStudyCompleted")
        if (isStudyCompleted) return
        UsageTrackingService.startOrRefresh(this)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
