package com.svoysport.tv.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.svoysport.tv.session.SessionManager
import com.svoysport.tv.ui.screens.auth.AuthScreen
import com.svoysport.tv.ui.screens.auth.CodeVerificationScreen
import com.svoysport.tv.ui.screens.details.DetailsScreen
import com.svoysport.tv.ui.screens.home.HomeScreen
import com.svoysport.tv.ui.screens.player.PlayerScreen
import com.svoysport.tv.ui.screens.profile.AboutScreen
import com.svoysport.tv.ui.screens.profile.DevicesScreen
import com.svoysport.tv.ui.screens.profile.ProfileScreen
import com.svoysport.tv.ui.screens.profile.SettingsScreen
import com.svoysport.tv.ui.screens.profile.SubscriptionScreen
import com.svoysport.tv.ui.screens.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Auth : Screen("auth")
    object CodeVerification : Screen("code_verification/{email}") {
        fun createRoute(email: String) = "code_verification/${email}"
    }
    object Details : Screen("details/{matchId}") {
        fun createRoute(matchId: String) = "details/$matchId"
    }
    object Player : Screen("player/{matchId}") {
        fun createRoute(matchId: String) = "player/$matchId"
    }
    object Profile : Screen("profile")
    object About : Screen("about")
    object Subscription : Screen("subscription")
    object Settings : Screen("settings")
    object Devices : Screen("devices")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onMatchClick = { matchId ->
                    navController.navigate(Screen.Details.createRoute(matchId))
                },
                onAuthClick = {
                    if (SessionManager.isLoggedIn.value) {
                        navController.navigate(Screen.Profile.route)
                    } else {
                        navController.navigate(Screen.Auth.route)
                    }
                },
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onClose = { navController.popBackStack() },
                onEmailSubmit = { email ->
                    navController.navigate(Screen.CodeVerification.createRoute(email))
                }
            )
        }

        composable(Screen.CodeVerification.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            CodeVerificationScreen(
                email = email,
                onClose = { navController.popBackStack() },
                onCodeComplete = { _ ->
                    SessionManager.isLoggedIn.value = true
                    SessionManager.userEmail.value = email
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Details.route) {
            DetailsScreen(
                onWatchClick = { matchId ->
                    navController.navigate(Screen.Player.createRoute(matchId))
                },
                onBack = { navController.popBackStack() },
                onLoginClick = { navController.navigate(Screen.Auth.route) }
            )
        }

        composable(Screen.Player.route) {
            PlayerScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onSubscriptionClick = { navController.navigate(Screen.Subscription.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onDevicesClick = { navController.navigate(Screen.Devices.route) },
                onAboutClick = { navController.navigate(Screen.About.route) },
                onLogout = {
                    SessionManager.isLoggedIn.value = false
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Subscription.route) {
            SubscriptionScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Devices.route) {
            DevicesScreen(onBack = { navController.popBackStack() })
        }
    }
}
