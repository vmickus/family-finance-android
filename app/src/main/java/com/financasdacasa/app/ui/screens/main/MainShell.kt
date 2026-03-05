package com.financasdacasa.app.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.financasdacasa.app.R
import com.financasdacasa.app.ui.components.BottomNavBar
import com.financasdacasa.app.ui.components.BottomNavTab
import com.financasdacasa.app.ui.screens.budgets.BudgetsScreen
import com.financasdacasa.app.ui.screens.categories.CategoriesScreen
import com.financasdacasa.app.ui.screens.home.HomeScreen
import com.financasdacasa.app.ui.screens.placeholder.PlaceholderScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "categories"

    Scaffold(
        topBar = {
            if (currentRoute in BottomNavTab.entries.map { it.route }) {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("categories") {
                                launchSingleTop = true
                            }
                        }) {
                            Icon(Lucide.Settings, contentDescription = stringResource(R.string.more))
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController)
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavTab.HOME.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(BottomNavTab.HOME.route) {
                HomeScreen()
            }
            composable(BottomNavTab.BUDGETS.route) {
                BudgetsScreen()
            }
            composable(BottomNavTab.GARDEN.route) {
                PlaceholderScreen(stringResource(R.string.nav_garden))
            }
            composable(BottomNavTab.DASHBOARD.route) {
                PlaceholderScreen(stringResource(R.string.nav_dashboard))
            }
            composable("categories") {
                CategoriesScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
