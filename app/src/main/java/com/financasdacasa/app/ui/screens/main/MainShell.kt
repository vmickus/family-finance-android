package com.financasdacasa.app.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.financasdacasa.app.R
import com.financasdacasa.app.ui.components.BottomNavBar
import com.financasdacasa.app.ui.components.BottomNavTab
import com.financasdacasa.app.ui.screens.placeholder.PlaceholderScreen

@Composable
fun MainShell() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavTab.HOME.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(BottomNavTab.HOME.route) {
                PlaceholderScreen(stringResource(R.string.nav_home))
            }
            composable(BottomNavTab.BUDGETS.route) {
                PlaceholderScreen(stringResource(R.string.nav_budgets))
            }
            composable(BottomNavTab.GARDEN.route) {
                PlaceholderScreen(stringResource(R.string.nav_garden))
            }
            composable(BottomNavTab.DASHBOARD.route) {
                PlaceholderScreen(stringResource(R.string.nav_dashboard))
            }
        }
    }
}
