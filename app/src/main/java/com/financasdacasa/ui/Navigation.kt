package com.financasdacasa.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.financasdacasa.ui.auth.AuthViewModel
import com.financasdacasa.ui.auth.LoginScreen
import com.financasdacasa.ui.auth.RegisterScreen
import com.financasdacasa.ui.dashboard.DashboardScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
}

@Composable
fun FinancasNavHost(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsState()

    val startDestination = if (uiState.isLoggedIn) Routes.DASHBOARD else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                viewModel = authViewModel
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                authViewModel = authViewModel
            )
        }
    }

    // Navigate based on auth state
    if (uiState.isLoggedIn && navController.currentDestination?.route != Routes.DASHBOARD) {
        navController.navigate(Routes.DASHBOARD) {
            popUpTo(0) { inclusive = true }
        }
    } else if (!uiState.isLoggedIn && navController.currentDestination?.route == Routes.DASHBOARD) {
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }
}
