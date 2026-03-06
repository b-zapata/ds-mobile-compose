package study.doomscrolling.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import study.doomscrolling.app.ui.screens.ConsentScreen
import study.doomscrolling.app.ui.screens.DashboardScreen
import study.doomscrolling.app.ui.screens.EligibilityScreen
import study.doomscrolling.app.ui.screens.OnboardingScreen

object AppDestinations {
    const val CONSENT = "consent"
    const val ELIGIBILITY = "eligibility"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.CONSENT
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppDestinations.CONSENT) {
            ConsentScreen(
                onAccept = { navController.navigate(AppDestinations.ELIGIBILITY) }
            )
        }
        composable(AppDestinations.ELIGIBILITY) {
            EligibilityScreen(
                onComplete = { navController.navigate(AppDestinations.ONBOARDING) }
            )
        }
        composable(AppDestinations.ONBOARDING) {
            OnboardingScreen(
                onComplete = { navController.navigate(AppDestinations.DASHBOARD) }
            )
        }
        composable(AppDestinations.DASHBOARD) {
            DashboardScreen()
        }
    }
}
