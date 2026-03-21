package study.doomscrolling.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import study.doomscrolling.app.BuildConfig
import study.doomscrolling.app.ui.screens.*

object AppDestinations {
    const val CONSENT = "consent"
    const val PERMISSIONS = "permissions"
    const val ELIGIBILITY = "eligibility"
    const val ONBOARDING = "onboarding"
    const val EXIT_SURVEY = "exit_survey"
    const val DASHBOARD = "dashboard"
    const val BASELINE_STATS = "baseline_stats"
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.DASHBOARD
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppDestinations.CONSENT) {
            ConsentScreen(
                onAccept = { navController.navigate(AppDestinations.DASHBOARD) }
            )
        }
        composable(AppDestinations.PERMISSIONS) {
            PermissionsScreen(
                onComplete = { navController.navigate(AppDestinations.DASHBOARD) }
            )
        }
        composable(AppDestinations.ELIGIBILITY) {
            EligibilityScreen(
                onComplete = { navController.navigate(AppDestinations.DASHBOARD) }
            )
        }
        composable(AppDestinations.ONBOARDING) {
            OnboardingScreen(
                onComplete = { navController.navigate(AppDestinations.DASHBOARD) }
            )
        }
        composable(AppDestinations.EXIT_SURVEY) {
            ExitSurveyScreen(
                onComplete = { navController.navigate(AppDestinations.DASHBOARD) }
            )
        }
        composable(AppDestinations.DASHBOARD) {
            DashboardScreen(
                onNavigateToConsent = { navController.navigate(AppDestinations.CONSENT) },
                onNavigateToPermissions = { navController.navigate(AppDestinations.PERMISSIONS) },
                onNavigateToEligibility = { navController.navigate(AppDestinations.ELIGIBILITY) },
                onNavigateToOnboarding = { navController.navigate(AppDestinations.ONBOARDING) },
                onNavigateToExitSurvey = { navController.navigate(AppDestinations.EXIT_SURVEY) },
                onOpenBaselineStats = {
                    if (BuildConfig.DEBUG) {
                        navController.navigate(AppDestinations.BASELINE_STATS)
                    }
                }
            )
        }
        if (BuildConfig.DEBUG) {
            composable(AppDestinations.BASELINE_STATS) {
                BaselineStatsScreen()
            }
        }
    }
}
