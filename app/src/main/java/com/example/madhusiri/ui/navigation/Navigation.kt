package com.example.madhusiri.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.madhusiri.ui.screens.*
import com.example.madhusiri.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object ProfileSetup : Screen("profile_setup")
    object Home : Screen("home")
}

@Composable
fun MadhuSiriNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                authViewModel = authViewModel,
                onNavigateToHome = {
                    authViewModel.startAlertService(context)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToSelection = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                viewModel = authViewModel,
                onComplete = {
                    authViewModel.startAlertService(context)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout(context)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
