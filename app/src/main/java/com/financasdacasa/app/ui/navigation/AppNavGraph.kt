package com.financasdacasa.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.financasdacasa.app.ui.screens.auth.LoginScreen
import com.financasdacasa.app.ui.screens.auth.RegisterScreen
import com.financasdacasa.app.ui.screens.house.HouseSelectionScreen
import com.financasdacasa.app.ui.screens.invite.InviteScreen
import com.financasdacasa.app.ui.screens.main.MainShell

object Routes {
    const val LOGIN = "login?inviteToken={inviteToken}"
    const val REGISTER = "register?inviteToken={inviteToken}"
    const val VERIFY_EMAIL = "verify-email"
    const val HOUSE_SELECTION = "house-selection?skipAutoSelect={skipAutoSelect}"
    const val HOME = "home"
}

private val houseSelectionArgs = listOf(
    navArgument("skipAutoSelect") {
        type = NavType.BoolType
        defaultValue = false
    },
)

@Composable
fun AuthNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "login") {
        composable(
            route = Routes.LOGIN,
            arguments = listOf(
                navArgument("inviteToken") {
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
                navArgument("inviteToken") {
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
fun InviteNavGraph(
    token: String,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = "invite/$token") {
        composable("invite/{token}") {
            InviteScreen(
                onNavigateToLogin = { inviteToken ->
                    navController.navigate("auth-login?inviteToken=$inviteToken") {
                        popUpTo("invite/$token") { inclusive = true }
                    }
                },
                onNavigateToRegister = { inviteToken ->
                    navController.navigate("auth-register?inviteToken=$inviteToken") {
                        popUpTo("invite/$token") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("house-selection") {
                        popUpTo("invite/$token") { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = "auth-login?inviteToken={inviteToken}",
            arguments = listOf(
                navArgument("inviteToken") {
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            LoginScreen(
                onNavigateToRegister = { invToken ->
                    val route = if (invToken != null) "auth-register?inviteToken=$invToken" else "auth-register"
                    navController.navigate(route)
                },
            )
        }

        composable(
            route = "auth-register?inviteToken={inviteToken}",
            arguments = listOf(
                navArgument("inviteToken") {
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            RegisterScreen(
                onNavigateToLogin = { invToken ->
                    val route = if (invToken != null) "auth-login?inviteToken=$invToken" else "auth-login"
                    navController.navigate(route) { popUpTo("auth-login") { inclusive = true } }
                },
            )
        }

        composable(
            route = Routes.HOUSE_SELECTION,
            arguments = houseSelectionArgs,
        ) {
            HouseSelectionScreen(
                onHouseSelected = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOUSE_SELECTION) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            MainShell(
                onNavigateToHouseSelection = {
                    navController.navigate("house-selection?skipAutoSelect=true") {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }
    }
}

@Composable
fun MainNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "house-selection") {
        composable(
            route = Routes.HOUSE_SELECTION,
            arguments = houseSelectionArgs,
        ) {
            HouseSelectionScreen(
                onHouseSelected = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOUSE_SELECTION) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            MainShell(
                onNavigateToHouseSelection = {
                    navController.navigate("house-selection?skipAutoSelect=true") {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }
    }
}
