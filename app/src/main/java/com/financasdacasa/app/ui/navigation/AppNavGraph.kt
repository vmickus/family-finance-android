package com.financasdacasa.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.financasdacasa.app.R
import com.financasdacasa.app.ui.screens.auth.LoginScreen
import com.financasdacasa.app.ui.screens.auth.RegisterScreen
import com.financasdacasa.app.ui.screens.auth.VerifyEmailScreen
import com.financasdacasa.app.ui.screens.house.HouseSelectionScreen

object Routes {
    const val LOGIN = "login?inviteToken={inviteToken}"
    const val REGISTER = "register?inviteToken={inviteToken}"
    const val VERIFY_EMAIL = "verify-email"
    const val HOUSE_SELECTION = "house-selection"
    const val HOME = "home"
}

@Composable
fun AuthNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "login") {
        composable(
            route = Routes.LOGIN,
            arguments = listOf(
                androidx.navigation.navArgument("inviteToken") {
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            LoginScreen(
                onNavigateToRegister = { inviteToken ->
                    val route = if (inviteToken != null) "register?inviteToken=$inviteToken" else "register"
                    navController.navigate(route)
                },
            )
        }

        composable(
            route = Routes.REGISTER,
            arguments = listOf(
                androidx.navigation.navArgument("inviteToken") {
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            RegisterScreen(
                onNavigateToLogin = { inviteToken ->
                    val route = if (inviteToken != null) "login?inviteToken=$inviteToken" else "login"
                    navController.navigate(route) { popUpTo("login") { inclusive = true } }
                },
            )
        }
    }
}

@Composable
fun MainNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOUSE_SELECTION) {
        composable(Routes.HOUSE_SELECTION) {
            HouseSelectionScreen(
                onHouseSelected = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOUSE_SELECTION) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            Scaffold { padding ->
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.home_placeholder))
                }
            }
        }
    }
}
